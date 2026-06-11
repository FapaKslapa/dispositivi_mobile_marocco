# Architecture

## Overview

DosageCalc follows **Clean Architecture** with three explicit layers: `domain`, `data`, and `presentation`. Each layer has a single direction of dependency: presentation depends on domain, data depends on domain, and domain depends on nothing.

```
presentation  ──►  domain  ◄──  data
```

---

## Layers

### Domain

The core of the application. Contains no Android dependencies - only pure Kotlin.

- **model/** - data classes and sealed classes (`Drug`, `Patient`, `DosageResult`, `Reminder`, …)
- **repository/** - interfaces that define contracts for data access (`DrugRepository`, `PatientRepository`, …)
- **usecase/** - one class per business operation (`CalculateDosageUseCase`, `CheckDrugInteractionsUseCase`, …)

Use cases own the business logic. A repository interface is the only dependency they accept.

### Data

Implements the repository interfaces and manages all I/O.

- **database/** - Room `AppDatabase` (SQLCipher-encrypted, version 8)
- **datasource/** - DAOs (`PatientDao`, `HistoryDao`, `ReminderDao`, `CustomDrugDao`) and `LocalDrugDataSource` for JSON assets
- **model/** - Room entity classes and `@Serializable` DTOs; each has a `toDomain()` conversion function
- **repository/** - concrete implementations (`PatientRepositoryImpl`, `DrugRepositoryImpl`, …)
- **di/** - Hilt `DataModule` that wires implementations to interfaces with `@Binds` and `@Provides`

### Presentation

MVVM with Jetpack Compose and `StateFlow`.

- **navigation/** - `AppNavigation` with a single `NavHost`; screens are composables, not fragments
- **calculator/**, **patient/**, **history/**, **onboarding/** - each sub-feature has its own `ViewModel`, `UiState` data class, component files, and screen files
- **ui/theme/** - Material 3 color scheme, typography, shapes, spacing, elevation tokens
- **ui/widget/** - two Jetpack Glance widgets

---

## Key Design Decisions

### Single shared ViewModel across the calculator flow

`DrugSelection → PatientInput → DosageResult` is a three-screen wizard that shares a single `CalculatorViewModel` scoped to the `NavHost`. This avoids serialising the selected drug and patient through navigation arguments and keeps the accumulated state (drug, weight, height, age, impairment flags, result) alive across the three screens without a shared repository call on every navigation event.

### Repository interface as the dependency boundary

Use cases depend only on interfaces. This makes unit-testing trivial: every use-case test instantiates the real use case and mocks only the repository. There is no mocking of business logic, which was the root cause of test/production divergence in previous projects.

### DataStore for lightweight preferences

Theme (dark/light) and BSA formula choice are stored in Jetpack DataStore rather than SharedPreferences. DataStore is `Flow`-based, so the UI reacts automatically without explicit reads. The `ThemeRepository` interface isolates the DataStore API from the rest of the app.

### `fallbackToDestructiveMigration`

The database uses `fallbackToDestructiveMigration(true)`. This is intentional for a prototype: a schema change wipes and rebuilds the database rather than requiring a migration script. In a production app this would be replaced by explicit `Migration` objects.

### `safePop()` in navigation

`NavHostController.popBackStack()` crashes if called after the back-stack entry has already left the `RESUMED` state (e.g. the user taps Back twice quickly). `safePop()` guards against this by checking the lifecycle state before popping.

---

## Database Schema

| Table | Description |
|-------|-------------|
| `patients` | Patient records (name, weight, height, age, impairment flags) |
| `history_records` | One row per calculation; foreign key to `patients` with `CASCADE DELETE` |
| `reminders` | Scheduled dose reminders |
| `custom_drugs` | User-created drug entries |

All dates are stored as `Long` epoch milliseconds. The domain model uses `LocalDateTime`; the mapping happens in the repository's `toDomain()` / `toEntity()` functions.

---

## Query Complexity

| Operation | Complexity | Notes |
|-----------|-----------|-------|
| `getAllHistory()` | O(n) | Full table scan ordered by date |
| `getAllHistoryPaged(query)` | O(n) amortised | Room `PagingSource` with a 20-row page; SQLite `LIKE` with leading wildcard prevents index use |
| `getHistoryForPatient(id)` | O(k) | Index on `patientId`; k = rows for that patient |
| `getPatientsPaged(query)` | O(n) | `LIKE '%query%'` on name/surname - no index |
| `checkInteraction(id1, id2)` | O(m) | Linear scan over the in-memory interactions list; m = number of known interactions |
| BSA calculation | O(1) | One or two arithmetic operations |
| Dose ceiling | O(1) | Single comparison |

The `LIKE '%query%'` pattern (leading wildcard) cannot use a B-tree index in SQLite. For the current data volume (hundreds of patients/records) this is not a bottleneck. A production version could use SQLite FTS5 for full-text search.

---

## Testing

Unit tests live in `app/src/test/` and use **JUnit 5** with **MockK** for mocking.

- `CalculateBsaUseCaseTest` - parameterised tests for Mosteller and Du Bois formulas against known reference values
- `CalculateDosageUseCaseTest` - per-kg calculation, dose ceiling capping, validation errors for out-of-range patients
- `CheckDrugInteractionsUseCaseTest` - interaction detection with mocked repository
