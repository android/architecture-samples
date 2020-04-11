# Android Architecture Blueprints - AAC + Dagger2 + Dagger-Android + Retrofit + UseCases
This project is based on [dagger-android|https://github.com/android/architecture-samples/tree/dagger-android]
branch which includes:
 - Retrofit 2x
 - UseCases Separation of Concern for implement Clean Architecture approach
 - Mock Flavour to be used for feature-switch (aka: feature-flag approach)
 - Added item menu to get and load Tasks from [json-placeholder-api|https://jsonplaceholder.typicode.com/posts]
 - Package name adjusting (to be more simple)

### References
 - [Android Architecture|https://github.com/android/architecture-samples/tree/dagger-android]
 - [Android References|https://github.com/amitshekhariitbhu/awesome-android-complete-reference/blob/master/README.md]
 - [Retrofit|https://proandroiddev.com/mvp-architecture-with-kotlin-dagger-2-retrofit-rxandroid-and-databinding-17bffe27393d]
 - [JSon Placeholder API|https://jsonplaceholder.typicode.com/posts]
 - [List filter|https://grokonez.com/kotlin/kotlin-list-filter-methods-example]
 - [Build variants|https://developer.android.com/studio/build/build-variants]
 - [Build disabled warnings|https://stackoverflow.com/questions/34692950/how-to-disable-android-studio-code-style-warning-can-be-simplified-to]

### Summary
My intention in ths project is provides some Kotlin and Android aspects that are not trivial for beginners and intermediate
programmers levels. So, this project can be used as study and can be used to set a new project without boilerplate configuration.
Have fun, enjoy a lot :)

### Original android Architecture Blueprints - AAC + Dagger2 + Dagger-Android

This sample is written in Kotlin and based on the
[master](https://github.com/googlesamples/android-architecture/tree/master) branch which uses
the following Architecture Components:
 - ViewModel
 - LiveData
 - Data Binding
 - Navigation
 - Room

It uses [Dagger 2](https://dagger.dev) and
[Dagger-Android](https://dagger.dev/android.html) for dependency injection. The Dagger setup is
deliberately simple and unopinionated.

### Differences with master

 - The ServiceLocator class is removed. Object creation and scoping is handled by Dagger.
 - Flavors `mock` and `prod` are no longer needed for testing so they're removed.


### Key files

The `di` directory contains all DI-related classes. This is done to improve browsing the files
but feature modules are usually placed alongside their packages (i.e. TaskDetailModule in
the `detail` package).

`ApplicationComponent` and its testing counterpart `TestApplicationComponent` define different
modules for production and UI testing.


### Testing

UI tests don't rely on using the `mock` flavor to run quickly and hermetically. Instead, they
replace Dagger components with their test versions.

This is done by creating a `CustomTestRunner`
which starts the `TestTodoApplication` instead of the `TodoApplication`. `DaggerTestApplicationRule`
creates the `TestApplicationComponent` and injects the Application.


### License


```
Copyright 2019 Google, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements. See the NOTICE file distributed with this work for
additional information regarding copyright ownership. The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
```
