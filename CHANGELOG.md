# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

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
- Feature: Implemented the ability to modify the DB data from the AdminPanel includin icons and colors from Cursos/Asignaturas.
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