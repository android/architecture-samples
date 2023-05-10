# Sample Multi-Module Android Project

This project is designed to showcase a variety of concepts and techniques used in modern Android
development. By exploring this sample, you can learn about:

1. **Building multiple apps from the same codebase**: The project demonstrates how to create 
separate apps for mobile and wearable devices using shared modules and features.
2. **Implementing many kinds of modules**: The sample includes various types of modules such as a
Kotlin-only module, a test-only module, an Android library, and a dynamic feature module. This helps
you understand how to structure different types of modules within an Android project.
3. **Navigation in a multi-module app**: The project shows how to manage navigation between modules
and features in a modular Android application.
4. **Consistent build configuration across many modules**: Using convention plugins and version
catalogs, this project maintains consistent build configurations and dependency management across
all modules.
5. **Feature modularization**: The project demonstrates how to break down the app into feature
modules, which allows for better separation of concerns, improves code reusability, and can
potentially enable dynamic feature delivery using the Android App Bundle.
6. **Testing strategies in a multi-module app**: The sample includes dedicated
[test modules](https://developer.android.com/studio/test/advanced-test-setup#use-separate-test-modules-for-instrumented-tests)
and testing utilities to showcase best practices for writing and organizing tests in a modular
Android app. Instrumentation tests within a feature module are designed to evaluate that specific
feature in isolation. On the other hand, instrumentation tests housed in a test module are intended
to assess multiple features and app modules collectively.
7. **Shared UI components**: The `:core:ui` module demonstrates how to create a shared library of
Jetpack Compose UI widgets that can be reused across multiple features and app variants.

By studying this project, you can gain valuable insights into modularizing your own Android app and
adopting best practices for code organization, navigation, build configuration, and testing. This
project follows best practices for Android app modularization. For more information on modularizing
your Android app, refer to the
[Guide to Android App Modularization](https://developer.android.com/topic/modularization).

## Modules Overview

The project is divided into several modules:

- `:app:mobile` - Android app module for phone devices.
- `:app:wear` - Android app module for wearable devices.
- `:build:logic:convention` - Conventions plugins for managing build configurations.
- `:core:testing` - Android library containing testing utilities.
- `:core:ui` - Android library with common Jetpack Compose UI widgets.
- `:core:util` - Kotlin-only module containing utility functions (not an Android library).
- `:data` - Android library for the data layer.
- `:dynamic` - Dynamic delivery module
- `:feature:details` - Android library for the details feature.
- `:feature:list` - Android library for the list feature.
- `:feature:wear:home` - Android library for the wear home feature.
- `:test:navigation` - Test-only module for navigation testing.
