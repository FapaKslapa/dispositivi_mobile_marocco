# Features

## Calculator Flow

The main user flow is a three-screen wizard: **Drug Selection → Patient Input → Dose Result**.

All three screens share a single `CalculatorViewModel`. The ViewModel holds the accumulated `CalculatorUiState` which contains the selected drug, patient inputs, impairment flags, and the calculation result.

---

## Dose Calculation

Handled by `CalculateDosageUseCase`. The formula used depends on the drug's `formulaType` field.

### PER_KG

```
dose = unitDose × weightKg
```

`unitDose` is in the drug's configured unit (e.g. µg/kg or mg/kg). Weight is entered by the user.

### PER_M2

```
dose = unitDose × BSA
```

BSA (Body Surface Area) is computed by `CalculateBsaUseCase` using the formula selected in settings.

**Mosteller formula:**
```
BSA = sqrt( (heightCm × weightKg) / 3600 )
```

**Du Bois formula:**
```
BSA = 0.007184 × heightCm^0.725 × weightKg^0.425
```

Both formulas return BSA in m², rounded to 4 decimal places. Mosteller is the default because it is the most widely used in clinical practice and produces results close to Du Bois for the typical adult range.

The Haycock formula is mentioned in the README as a third option but it maps to Du Bois in the current implementation (`BsaFormulaType` only has `MOSTELLER` and `DU_BOIS`).

### FIXED

```
dose = unitDose
```

The dose is constant regardless of patient weight or height.

### BY_RANGE

Calculates a minimum and maximum dose using `unitDose` and `unitDoseMax`:

```
minDose = unitDose × weightKg
maxDose = unitDoseMax × weightKg
```

The result is returned as a range (`totalDose` to `totalDoseMax`).

---

## Organ Impairment Adjustments

If the patient has renal or hepatic impairment the base dose is multiplied by an impairment factor before the ceiling is applied.

### Renal adjustment

The drug defines a `renalDoseMultiplier` (the target multiplier at maximum impairment, i.e. G5). For intermediate stages the factor is interpolated linearly:

| Stage | Interpolation weight (t) |
|-------|--------------------------|
| G1 (none) | 0.00 → factor = 1.0 |
| G2 | 0.10 |
| G3 | 0.35 |
| G4 | 0.75 |
| G5 | 1.00 → factor = renalDoseMultiplier |

The interpolation formula is `lerp(1.0, multiplier, t)` = `1.0 + (multiplier - 1.0) × t`.

If no `renalDoseMultiplier` is defined for the drug, a warning is appended to the alert text ("no adjustment defined in official documentation").

### Hepatic adjustment

Same mechanism with Child-Pugh stages and different interpolation weights:

| Stage | t |
|-------|---|
| None | 0.00 |
| Child A | 0.25 |
| Child B | 0.65 |
| Child C | 1.00 → factor = hepaticDoseMultiplier |

---

## Dose Ceiling

If the drug defines `maxSingleDoseMcg`, the computed dose is capped:

```
finalDose = min(computedDose, maxSingleDoseMcg)
```

When capping occurs, `DosageResult.Success.cappedToMaxDose` is set to `true`. The result screen and the PDF report both display a warning when this flag is set.

---

## Input Validation

`ValidateInputUseCase` runs before the calculation and collects errors:

- Weight is required for `PER_KG` and `BY_RANGE` formulas.
- Both weight and height are required for `PER_M2` (BSA needs both).
- Physiological range: weight 1–500 kg, height 30–280 cm, age 0–120 years.
- Drug-specific constraints: `minWeightKg`, `maxWeightKg`, `minAgeYears`.

Errors are joined into a single `DosageResult.ValidationError` with a bullet-list message shown on screen.

---

## Drug-Drug Interaction Detection

When a patient is selected, `CheckDrugInteractionsUseCase` looks up the patient's calculation history and checks each previously used drug against the currently selected drug.

The interaction data comes from `assets/interactions.json`. Each entry has two drug IDs, a risk level (`LOW`, `MODERATE`, `HIGH`), and a description. The check is symmetric (A→B or B→A both match).

The lookup is O(k × m) where k = number of distinct drugs in the patient's history and m = total known interactions. For the current dataset size (tens of entries) this is negligible.

---

## Drug Catalog

The built-in catalog is loaded from `assets/drugs.json` at first access (lazy) by `LocalDrugDataSource`. Each drug entry maps to a `DrugDto` which is converted to the domain `Drug` model via `toDomain()`.

Custom drugs are stored in the `custom_drugs` Room table. `DrugRepositoryImpl.getDrugs()` merges both sources: it takes the static list from `LocalDrugDataSource.drugs` and combines it with the live `Flow` from `CustomDrugDao.getAllCustomDrugs()`. The merged list is sorted by name.

Custom drugs can be created, edited (re-inserted with the same ID via `OnConflictStrategy.REPLACE`), and deleted.

---

## Patient Management

Patient records are stored in the `patients` table. The `PatientsViewModel` uses `Paging 3` to load patients in pages of 20, with an `OutlinedTextField` search bar that filters by name and surname.

When a patient is selected in the calculator, their weight, height, age, and impairment flags are pre-filled into the `CalculatorUiState`. If the user manually edits any field the patient is deselected so the link between stored data and the current input is broken.

---

## History and Analytics

Every successful calculation is saved as a `HistoryRecord` with the patient ID (nullable for anonymous calculations), drug name and ID, date, anthropometric values, and the result.

The history screen uses a paged `LazyColumn`. The analytics screen shows:

- **Dose trend chart** - a line chart for a selected drug + patient combination, built with Canvas drawing (no chart library).
- **Category distribution** - a bar/percentage summary of how many calculations belong to each drug category.

---

## Export

### PDF

`PdfManager` uses the Android `PdfDocument` API (no third-party library). The PDF is generated entirely in memory with Canvas operations: gradient header, patient card, dose hero card, formula card, and optional alert card. The file is written to `context.cacheDir` and shared via `FileProvider` + `Intent.ACTION_SEND`.

### CSV

`ExportManager.exportHistoryToCsv()` builds a plain CSV string (comma-separated, UTF-8) with columns: date, drug name, patient ID, weight, calculated dose, unit, formula. The file is shared the same way as the PDF.

---

## Reminders

Reminders are created from the dose result screen. The user picks an interval (daily / weekly / monthly), a time (hour + minute), and a duration in days.

`ReminderManager.scheduleReminder()` loops from 0 to `duration - 1` and enqueues one `OneTimeWorkRequest` per occurrence, each with an `initialDelay` computed from the `Calendar` API. Weekly and monthly reminders advance to the next occurrence if the chosen time is already past for the current period.

Each reminder series shares a tag (`seriesId`) so all its work requests can be cancelled atomically with `cancelAllWorkByTag()`.

`ReminderWorker` fires a `NotificationCompat` notification. On Android 13+ it first checks for `POST_NOTIFICATIONS` permission.

---

## Widgets

Two Jetpack Glance widgets are available:

- **LastDrugWidget** - shows the drug name and calculated dose from the most recent history record.
- **NextReminderWidget** - shows the time of the next scheduled reminder (the first reminder whose time is still in the future in the current day, or the first reminder of the next day if all have passed).

Both widgets tap to open `MainActivity`. `WidgetUpdateHelper.updateAllWidgets()` is called after every calculation and after every reminder add/delete so the widgets stay fresh without polling.

---

## Responsive Layout

The app supports four form factors: phone portrait, phone landscape, foldable, and tablet.

- `isCompactHeight()` - true when the device is a phone in landscape; used to compress tall headers.
- `isMediumOrExpandedWidth()` - true on tablets and large foldables; used to switch to side-by-side layouts.
- `isExpandedWidth()` - true on tablets in landscape (width class = Expanded); used for two-column grids.
- `responsiveContentWidth()` - a `Modifier` extension that caps content width at 640 dp; applied to all forms and detail screens so they don't stretch to the full tablet width.

---

## Onboarding

`OnboardingViewModel` reads `OnboardingRepository.isCompleted` which is backed by a DataStore `Boolean`. The initial value is `null` (not yet emitted). `AppNavigation` shows a loading spinner while the value is null, then routes to `OnboardingScreen` or `DrugSelectionScreen` depending on the result. Once the user finishes onboarding, `markCompleted()` writes `true` to DataStore and the app never shows the onboarding screen again.

---

## Theme

The app has three theme modes: light, dark, and system (system default is dark on first launch). The choice is persisted in DataStore via `ThemeRepository`. `MainViewModel` exposes `isDarkTheme: StateFlow<Boolean>` which is consumed by `MainActivity` to pass `darkTheme` to `DosageCalcTheme`.
