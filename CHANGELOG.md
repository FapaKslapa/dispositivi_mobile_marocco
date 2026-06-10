# Changelog

All notable changes to DosageCalc are documented here.  
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/);  
versioning follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.4.0] — 2026-06-10

### Added
- Macrobenchmark module for baseline profile generation.
- `DrugCategoryFilterRow` reusable filter chip component.
- `DrugFormatUtils` shared formatting utilities (`formatDose`, `label`).

### Changed
- Detekt static analysis configured and active on every CI run.
- `RemindersSheet` split into focused private composables (`ActiveRemindersSection`, `ReminderCard`).
- `DeleteDrugDialog` extracted from `DrugSelectionScreen`.
- CI workflow now runs ktlint, detekt, tests and lint; release workflow simplified.
- `WidgetDataProvider` migrated to Hilt `EntryPointAccessors`.

### Fixed
- Unsafe `InteractionRiskLevel.valueOf()` replaced with safe `runCatching` fallback.
- Duplicate UUID generation removed from `HistoryRepositoryImpl`.
- Leading space in reminder notification text.
- `CalculatorViewModel` nested coroutine launch removed.

---

## [1.1.1] — 2026-04-10

### Fixed
- Release APK is now signed with the debug key so it can be installed directly on any Android device.

---

## [1.1.0] — 2026-04-10

### Added
- Responsive layout: portrait, landscape and tablet support.

---

## [1.0.0] — 2026-04-10

### Added

#### Core calculation engine
- Drug dosage calculator with `per_kg`, `per_m2`, and `fixed` formula types
- BSA (Body Surface Area) calculation with multiple formula variants (Mosteller, DuBois, Haycock)
- Dose ceiling safety: calculated doses are capped at `maxSingleDoseMcg` and the user is always notified when a cap is applied
- Drug-drug interaction detection with clinical severity levels
- BY_RANGE formula skeleton (returns explicit error; not yet implemented)

#### Drug catalog
- Built-in drug catalog loaded from `assets/drugs.json` (sources: RCP, AIFA, WHO)
- Drug detail screen displaying full RCP data (indications, posology, contraindications)
- Custom drug management: add, edit, and delete user-defined drugs

#### Patient management
- Patient input screen with weight, height, age, and sex fields
- Patient history stored in local encrypted database (SQLCipher)
- Patient detail and update screens

#### History & analytics
- Dose history log per patient
- Analytics screen with dose trend chart

#### Export
- Export dose history as PDF or CSV

#### Reminders
- Dose reminder scheduling with local notifications
- Next-reminder widget (Jetpack Glance)
- Last-drug widget (Jetpack Glance)

#### UI / UX
- Material 3 theme with dynamic color support
- Light / Dark / System theme switcher
- Smooth navigation animations between screens
- Navigation safety guards (back-stack deduplication)

#### Architecture
- Clean Architecture (domain / data / presentation)
- Hilt dependency injection
- Jetpack Compose + MVVM
- Shared ViewModel across the three-screen calculator flow (DrugSelection → PatientInput → DosageResult)
- JUnit 5 + MockK + Turbine unit test suite

---

[1.4.0]: https://github.com/FapaKslapa/dosage_calc/releases/tag/v1.4.0
[1.1.1]: https://github.com/FapaKslapa/dispositivi_mobile_marocco/releases/tag/v1.1.1
[1.1.0]: https://github.com/FapaKslapa/dispositivi_mobile_marocco/releases/tag/v1.1.0
[1.0.0]: https://github.com/FapaKslapa/dispositivi_mobile_marocco/releases/tag/v1.0.0
