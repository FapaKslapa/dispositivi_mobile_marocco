# Libraries

## UI

### Jetpack Compose

The entire UI is written in Compose - no XML layouts. Key patterns used:

- **`StateFlow` → `collectAsStateWithLifecycle()`** collects state inside a composable, automatically stopping collection when the composable leaves the `STARTED` lifecycle state. This avoids unnecessary work while the app is in the background.
- **`hiltViewModel()`** injects the right ViewModel scoped to the current back-stack entry. When the same ViewModel instance is needed across multiple screens (the calculator flow), it is obtained by calling `hiltViewModel()` at the `NavHost` level and passed down as a parameter.
- **`AnimatedVisibility` / `AnimatedContent`** handle enter/exit transitions for cards and lists. Spring-based animations are used for interactive elements (card reveal); tween for screen transitions.
- **`CompositionLocalProvider`** exposes `WindowSizeClass`, `Spacing`, `Elevation`, and `DosageShapes` throughout the tree without passing them explicitly to every composable.

### Jetpack Glance

Used for the two home-screen widgets (`LastDrugWidget`, `NextReminderWidget`). Glance is a Compose-based widget API built on `RemoteViews` under the hood.

Because widgets run in a separate process they cannot use Hilt or observe `Flow` directly. `WidgetDataProvider` opens the database directly via `AppDatabase.getInstance(context)` and reads the first emission from the DAO `Flow` with `.first()`. `WidgetUpdateHelper` calls `GlanceAppWidgetManager.getGlanceIds()` and `update()` after every calculation or reminder change.

### Material 3 (`androidx.compose.material3`)

The app uses a hand-crafted Material 3 color scheme (not dynamic color) defined in `Color.kt`. The theme exposes custom `CompositionLocal`s for spacing, elevation, and shape tokens beyond the standard Material tokens. Dynamic color is kept as a `dynamicColor` parameter in `DosageCalcTheme` but is disabled by default.

---

## Persistence

### Room (`androidx.room`)

Room generates the DAO implementations and handles the SQL boilerplate. The app uses:

- `@Query` with `Flow<List<T>>` return type for reactive lists - Room re-emits on every write.
- `@Query` with `PagingSource<Int, T>` return type for paginated screens (history, patients).
- `ForeignKey(onDelete = CASCADE)` on `history_records.patientId` so that deleting a patient automatically removes all their history rows.
- `exportSchema = true` produces the JSON schema files in `app/schemas/`, which serve as a migration history.

### SQLCipher (`net.zetetic:android-database-sqlcipher`)

All Room tables are stored in an encrypted SQLite database. The passphrase is passed to `SupportOpenHelperFactory` at database construction time. The native library is loaded with `System.loadLibrary("sqlcipher")` in the Hilt `DataModule`.

Note: the passphrase is currently hardcoded. For a production release it should be derived from a key stored in the Android Keystore.

### Jetpack DataStore (`androidx.datastore`)

Stores two preferences: dark/light theme choice (`is_dark_theme`) and the selected BSA formula (`bsa_formula`). DataStore is type-safe, coroutine-friendly, and avoids the thread-safety issues of `SharedPreferences`. Both values are read as `Flow<T>` and exposed through `ThemeRepository`.

---

## Background Work

### WorkManager (`androidx.work`)

Reminder notifications are implemented as one-shot `OneTimeWorkRequest`s, one per occurrence, scheduled with an `initialDelay`. Each request is tagged with a `seriesId` so all occurrences belonging to one reminder can be cancelled together with `cancelAllWorkByTag()`.

`ReminderWorker` is a `CoroutineWorker` injected via `@HiltWorker` and `@AssistedInject`. The `DosageCalcApplication` provides a custom `WorkManager` configuration that sets `HiltWorkerFactory` so Hilt can inject into workers.

---

## Dependency Injection

### Hilt (`dagger.hilt`)

Hilt is the standard DI solution for Android. The app uses:

- `@HiltAndroidApp` on `DosageCalcApplication`
- `@AndroidEntryPoint` on `MainActivity`
- `@HiltViewModel` + `@Inject constructor` on every ViewModel
- `@HiltWorker` + `@AssistedInject` on `ReminderWorker`
- `@Module @InstallIn(SingletonComponent)` for the `DataModule`

The `DataModule` uses `@Binds` to map repository interfaces to their implementations and `@Provides` to construct objects that require non-trivial setup (database, DataStore, JSON decoder).

---

## Data Serialization

### Kotlinx.serialization (`org.jetbrains.kotlinx:kotlinx-serialization-json`)

The built-in drug catalog (`assets/drugs.json`) and interaction list (`assets/interactions.json`) are parsed with `kotlinx.serialization`. The `Json` instance is configured with `ignoreUnknownKeys = true` so new fields added to the JSON do not break old app versions. DTOs are annotated with `@Serializable`.

---

## Pagination

### Paging 3 (`androidx.paging`)

Patient and history lists use `Pager` + `PagingConfig(pageSize = 20)`. The DAOs return `PagingSource<Int, Entity>` which Room generates automatically from `@Query` methods. The ViewModels expose `Flow<PagingData<DomainModel>>` using `cachedIn(viewModelScope)` to survive configuration changes. The UI uses `collectAsLazyPagingItems()`.

---

## Navigation

### Navigation Compose (`androidx.navigation.compose`)

The app uses a single `NavHost` with string-based routes (`AppRoute` sealed class). Optional parameters are passed as query strings (`?patientId={patientId}`) with `nullable = true` so screens that don't need a parameter reuse the same route pattern.

All screen transitions use the same animation spec (slide + fade, 280 ms) defined on the `NavHost` level to ensure consistency.

---

## Window Size

### Material 3 Window Size (`androidx.compose.material3:material3-window-size-class`)

`calculateWindowSizeClass()` is called once in `MainActivity` and propagated down via `CompositionLocalProvider(LocalWindowSizeClass)`. Helper functions in `WindowSize.kt` (`isCompactHeight()`, `isMediumOrExpandedWidth()`, `isExpandedWidth()`) read from this local so any composable can adapt its layout without receiving `WindowSizeClass` as a parameter. `responsiveContentWidth()` is a `Modifier` extension that caps width to 640 dp on tablets.
