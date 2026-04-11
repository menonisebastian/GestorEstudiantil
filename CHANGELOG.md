# Changelog

All notable changes to this project will be documented in this file.

## [v0.3.6] - 2026-04-15
- Feature: Fully implemented Recordatorios (Reminders) CRUD across all layers (Firestore, Repository, ViewModel, and UI).
- UI Improvement: Enhanced `CustomNotificationCard` with a visual "clip" adornment and standardized date formatting using `formatearFechaParaMostrar`.
- UI Interaction: Integrated full edit/delete lifecycle for reminders in `RecordatoriosEstudiantePanel` and `RecordatoriosProfesorPanel`.
- Dialog Management: Added `EditRecordatorioDialog` to `DialogOrchestrator` for consistent state-driven editing.
- Infrastructure: Implemented `actualizarRecordatorio` in `RecordatorioRepository` with Firestore-backed atomic updates.
- Infrastructure: Migrated push notification logic from Cloud Functions to direct Firebase API integration for improved efficiency.
- Refactor: Wired `AppViewModel` reminder actions to `HomeScreen` callbacks to ensure reactive UI updates across different user roles.

## [v0.3.5] - 2026-04-14
- Feature: Implemented push notification system using Firebase Cloud Messaging and Cloud Functions.
- Navigation: Internal "Deep Linking" support to open subject details from notifications.
- Infrastructure: Dynamic subscription to Firebase topics based on student enrollment.
- UI: Implemented intent handling in MainActivity and synchronization with Navigation 3 backstack.

## [v0.3.4] - 2026-04-13
- Feature: Implemented Firebase password recovery flow.
- UI Improvement: Added `ForgotPasswordPanel` and `ForgotPasswordSuccessPanel` with automated navigation and success state feedback.
- Navigation: Integrated password recovery into the `AuthScreen` internal backstack using Navigation 3, ensuring a clean return to Login.
- Infrastructure: Extended `AuthRepository` and `AuthViewModel` to support password reset email triggers.

## [v0.3.3] - 2026-04-13
- UI Improvement: Replaced dropdown filters with a multi-selectable `FilterChip` system in `FilterByDialog` for a more modern and touch-friendly experience.
- UI Improvement: Enhanced `CentrosAdminPanel` shift selection with contextual icons (Sun for Morning, Twilight for Evening).
- UI Improvement: Standardized schedule headers in Admin panel to follow the "Acronym + Shift + Cycle" format (e.g., "DAM M2").
- UI Improvement: Updated `MateriaDetalleEstudiantePanel` header with an interactive teacher profile row replacing the static text.
- Bugfix: Resolved Firestore deserialization crashes in `Asignatura` (@DocumentId naming collision) and `User` (missing fields).
- Bugfix: Corrected data type mismatch for `cicloNum` when generating new schedule entries in `HorariosAdminScreen`.
- Data Integrity: Synchronized `imgUrl` and `fotoUrl` fields across Registration Use Cases and Profile updates to ensure avatar consistency.
- Infrastructure: Extended `ProfesorRepository` and `ProfesorViewModel` to support fetching teacher profiles for student detail views.

## [v0.3.2] - 2026-04-12
- Admin UI Improvements: Standardized icons in `CentrosAdminPanel` for FP levels (Specialization: `AutoAwesome`, Superior: `School`, Middle: `MenuBook`, Basic: `ImportContacts`).
- Admin UI Improvements: Refactored Course selection in user editing (Dialog and Screen) from manual text to a database-driven dropdown of acronyms (e.g., "DAM", "DAW").
- Admin UI Improvements: Synchronized `cursoId` and `cursoOArea` in user editing; selecting a course now automatically updates both the internal ID and the visual label (e.g., "DAMV1").
- Component Standardization: Unified `CustomOptionsTextField` visual style with `CustomTextField` (surfaceColor, 16dp corners, borderless) and fixed dropdown menu clipping/colors.
- Data Integrity: Added reactive loading of course data (`cargarCursosPorCentro`) when entering user management or editing screens to ensure dropdown availability.
- Bugfix: Corrected `DialogState.EditUser` implementation in `HomeScreen.kt` and `UsuariosAdminPanel.kt` to pass the full `List<Curso>` instead of just acronyms.

## [v0.3.1] - 2026-04-11
- UI Standardization: Replaced all `OutlinedTextField` instances with a unified `CustomTextField` featuring a borderless design, 16dp rounded corners, and `surfaceColor` background.
- Admin Management: Implemented `EditUserDialog` for comprehensive user editing, featuring role-based conditional fields (Shift, Cycle, Course/Area).
- Admin Management: Inverted the tab order in `UsuariosAdminPanel` to display "Activos" (Active) as the primary tab.
- Visual Grid: Refactored Subject panels from horizontal `LazyRow` to a 3-column `LazyVerticalGrid` to improve accessibility and information density.
- Typography & Layout: Enhanced `CardItem` legibility with improved text contrast, centered alignment, and ellipsis handling for long subject names.
- Component Refactor: Updated `CustomSearchBar` and `CustomPasswordTextField` (migrated to `TextFieldState`) to maintain visual and functional consistency across the app.

## [v0.3.0] - 2026-04-10
- Navigation Persistence: Implemented `tabBackStacks` (map of `SnapshotStateList`) in `HomeScreen` to maintain independent navigation states for each bottom bar tab.
- Scroll Control: Disabled `HorizontalPager` swiping on detail routes (`isDetailRoute`) to prevent accidental tab switching during nested navigation.
- Centralized UI Management: Unified Floating Action Button (FAB) logic and backstack management within the main `HomeScreen` Scaffold for consistent behavior across all Admin levels.
- UI Consistency: Standardized all navigation icons to `Icons.AutoMirrored.Filled.ArrowBackIos` and `Icons.AutoMirrored.Filled.ArrowForwardIos` across the entire application.
- Visual Improvements: Updated `CursoCard` to be fully model-driven, using `iconoName`, `colorFondoHex`, and `colorIconoHex` from the `Curso` data object.
- UX Refinement: Added `80.dp` bottom content padding to all administrative and grading `LazyColumn` lists to prevent the FAB from obscuring items.
- Bugfix: Fixed redundant header titles in `CursosScreen` (e.g., "Cursos de Curso de...") by conditionally checking if the type name already contains the word "Curso".

## [v0.2.9] - 2026-04-10
- Navigation Architecture: Centralized all Admin sub-routes (Centros, Tipos, Cursos, Asignaturas, Usuarios) in `HomeScreen` to resolve `IllegalStateException` and "Unknown screen" crashes caused by nested `NavDisplay` components.
- Navigation Architecture: Updated `NestedNavMappers` to correctly identify detail routes within the expanded Admin hierarchy to support back-gesture priority.
- State Management: Scoped `AdminViewModel` and `ProfesorViewModel` at the `HomeScreen` level to ensure state persistence and shared data across sub-navigation levels.
- Refactor: Decoupled `CentrosAdminPanel` into stateless, reusable screen components (`CentrosListScreen`, `TiposCursoScreen`, `CursosScreen`, etc.).
- Improvement: Internalized search and UI state within `EstudiantesAsignaturaLista` (Calificaciones) for better encapsulation and simplified parent logic.
- Bugfix: Fixed icon import issues and type inference errors in the Admin navigation tree.

## [v0.2.8] - 2026-04-10
- Bugfix: Fixed student notification badge not appearing on new or edited posts.
- Feature: Added `fechaActualizacion` to Posts to track edits and trigger notifications.
- Improvement: Notification count now only considers visible posts.
- Infrastructure: Updated `EstudianteRepository` and `ProfesorRepository` to handle post update timestamps.

## [v0.2.7] - 2026-04-10
- Architecture: Completed full refactor to Clean Architecture + MVVM.
- Architecture: Implemented Repository Pattern for all data sources (Auth, User, Course, Admin, Estudiante, Profesor, Recordatorio).
- Architecture: Introduced Domain layer with Use Cases for complex business logic (Registration, Database Seeding, Notifications, etc.).
- Dependency Injection: Migrated all ViewModels to Hilt (`@HiltViewModel`).
- Dependency Injection: Created `FirebaseModule` and `RepositoryModule` for Hilt provider/binding management.
- Improvement: ViewModels now use `StateFlow` for state management and are decoupled from Firebase SDKs.
- Improvement: Removed `ListenerRegistration` manual handling in ViewModels by using `callbackFlow` in Repositories.
- Refactor: Standardized Hilt ViewModel injection using `hiltViewModel()` across all screens, removing manual ViewModel passing in `AppNavigation`.
- Bugfix: Fixed parameter mismatch and field naming in Google Setup registration flow.
- Fix: Resolved build errors related to AGP 9.1 and Kotlin sourceSet configurations.

## [v0.2.6] - 2026-04-10
- Feature: Integrated Hilt for Dependency Injection.
- Infrastructure: Configured KSP (Kotlin Symbol Processing) for Hilt code generation.
- Infrastructure: Added `GestorEstudiantilApp` and `@AndroidEntryPoint` in `MainActivity`.
- Infrastructure: Added `hilt-navigation-compose` for ViewModel integration in Compose.

## [v0.2.5] - 2026-04-10
- Feature: Schedule is now fully data-driven from Firestore.
- Feature: Schedule synchronization between student and teacher schedules.
- Feature: Implemented `HorariosProfesorPanel` for teacher schedules.
- Feature: Added full subject names and group codes (e.g., "DAM V1") to schedule views for both roles.
- Improvement: Standardized "turno" to lowercase across the entire system (Auth, Admin, Student) to resolve Firestore case-sensitivity issues.
- Improvement: Enhanced schedule synchronization in `HomeScreen` to reactively load data when user profile fields (turno, cicloNum) are updated.
- Improvement: Refactored `HorariosEstudiantePanel` with better loading states, empty states, and more flexible time slot matching.
- Bugfix: Fixed a critical issue where schedules wouldn't display due to casing discrepancies and incomplete user profiles during initial data load.
- Improvement: Logo and branding updated.

## [v0.2.4] - 2026-04-08
- Feature: Unified "Centros" and "Asignación" into a single administrative flow.
- Improvement: Converted all administrative edit dialogs (Centros, Cursos, Asignaturas, Usuarios) into full-screen Scaffold views.
- Improvement: Redesigned edit screens with fixed bottom bars for primary actions (Save/Cancel).
- Improvement: Navigation 3 routes and mappers updated to support type-safe full-screen editing.
- Improvement: Extracted UI picker components (ColorPicker, IconPicker) into a shared library.
- Improvement: Logo and branding updated.
- Improvement: Redesigned the UI for the Authentication screens.
- Change: Removed obsolete `AdminEditDialogs.kt`.

## [v0.2.3] - 2026-04-07
- Feature: Implemented the ability to add new Cursos/Asignaturas.
- Feature: Implemented the ability to make Unidades/Posts inside Asignaturas.
- Feature: Implemented the ability to qualify students.
- Feature: Implemented the ability to add Recordatorios.
- Bugfix: Fixed the matriculation of students.
- Change: Cursos/Asignaturas IDs are now deterministic.
- Change: cursoOArea is now a concatenation of the course + turn + cycle.


## [v0.2.2] - 2026-04-07
- Bugfix: Fixed the raw data from DB and .jsonl.
- Change: Modified AdminViewModel and Navigation.
- Feature: Filter chips for the SearchBar
- Feature: Creation of Profesor panels.
- Feature: Implemented the ability to modify the DB data from the AdminPanel including icons and colors from Cursos/Asignaturas.
- Change: Cursos/Asignaturas now have their own colors.

## [v0.2.1] - 2026-03-15
- Improvement: Reestructured NavigationBar to be a nested navigation with backstacks.

## [v0.2.0] - 2026-03-15
- Fixed: Fixed a bug in the Google OAuth login when the user's role was Student.
- Improvement: Reestructured NavDisplay & Routes.

## [v0.1.9] - 2026-03-09
- Improvement: Added a new data provider for IES Comercio.
- Improvement: Added CentrosAdminPanel.

## [v0.1.8] - 2026-03-09
- Improvement: Dialogs are now managed by DialogState and DialogOrchestrator.
- Improvement: CustomOptionsTextField now using the ExposedDropdownMenu library.

## [v0.1.7] - 2026-03-08
- Improvement: NavHost is now NavDisplay using Navigation 3.

## [v0.1.6] - 2026-03-04
- Improvement: Search bar filter dialog implemented.

## [v0.1.5] - 2026-03-04
- Bugfix: Fixed Google OAuth login.
- Improvement: Encapsulated Google OAuth login.
- Improvement: Google Screen now also asks for school data.
- Change: Updated third-party dependencies.
- Change: Form Screens are now ConstraintLayout.

## [v0.1.3] - 2026-03-01
- Feature: Added user authentication.
- Bugfix: Fixed layout issues on the dashboard.

## [v0.1.2] - 2026-02-15
- Improvement: Enhanced performance of data fetching.
- Change: Updated third-party dependencies.

## [v0.1.1] - 2026-02-01
- Feature: Implemented a new UI design for user profile.

## [v0.0.1] - 2026-01-01
- Initial release with core features.