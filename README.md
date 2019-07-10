# Android Architecture Blueprints v2
<p align="center">
<img src="https://github.com/googlesamples/android-architecture/wiki/images/aab-logov2.png" alt="Android Architecture Blueprints"/>
</p>

Android Architecture Blueprints is a project to showcase different architectural approaches to developing Android apps. In its different branches you'll find the same app (a TODO app) implemented with small differences.

In this branch you'll find:
*   Kotlin **[Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html)** for background operations.
*   A single-activity architecture, using the **[Navigation component](https://developer.android.com/guide/navigation/navigation-getting-started)** to manage fragment operations.
*   A presentation layer that contains a fragment (View) and a **ViewModel** per screen (or feature).
*   Reactive UIs using **LiveData** observables and **Data Binding**.
*   A **data layer** with a repository and two data sources (local using Room and remote) that are queried with one-shot operations (no listeners or data streams).
*   Two **product flavors**, `mock` and `prod`, [to ease development and testing](https://android-developers.googleblog.com/2015/12/leveraging-product-flavors-in-android.html) (except in the Dagger branch).
*   A collection of unit, integration and e2e **tests**, including "shared" tests that can be run on emulator/device or Robolectric.

## Variations

This project hosts each sample app in separate repository branches. For more information, see the `README.md` file in each branch.

### Stable samples - Kotlin
|     Sample     | Description |
| ------------- | ------------- |
| [master](https://github.com/googlesamples/android-architecture/tree/master) | The base for the rest of the branches. <br/>Uses Kotlin, Architecture Components, coroutines, Data Binding, etc. |
| [dagger-android](https://github.com/googlesamples/android-architecture/tree/dagger-android)<br/>[[compare](https://github.com/googlesamples/android-architecture/compare/dagger-android#files_bucket)] | A simple Dagger setup that uses `dagger-android` and removes the two flavors. |
| [usecases](https://github.com/googlesamples/android-architecture/tree/usecases)<br/>[[compare](https://github.com/googlesamples/android-architecture/compare/usecases#files_bucket)] | Adds a new domain layer that uses UseCases for business logic. |

### Samples in development - Kotlin

| Sample | Description |
| ------------- | ------------- |
| [reactive](https://github.com/googlesamples/android-architecture/tree/reactive)<br/>[[compare](https://github.com/googlesamples/android-architecture/compare/reactive#files_bucket)] | Modifies the data layer so UIs react to changes automatically using Room as source of truth. |

### Old samples - Kotlin and Java

Blueprints v1 had a collection of samples that are not maintained anymore, but can still be useful. See [all project branches](https://github.com/googlesamples/android-architecture/branches).

## Why a to-do app?

<img align="right" src="https://github.com/googlesamples/android-architecture/wiki/images/todoapp.gif" alt="A demo illustraating the UI of the app" width="288" height="512" style="display: inline; float: right"/>

The app in this project aims to be simple enough that you can understand it quickly, but complex enough to showcase difficult design decisions and testing scenarios. For more information, see the [app's specification](https://github.com/googlesamples/android-architecture/wiki/To-do-app-specification).

## What is it not?

*   A UI/Material Design sample. The interface of the app is deliberately kept simple to focus on architecture. Check out [Plaid](https://github.com/android/plaid) instead.
*   A complete Jetpack sample covering all libraries. Check out [Android Sunflower](https://github.com/googlesamples/android-sunflower) or the advanced [Github Browser Sample](https://github.com/googlesamples/android-architecture-components/tree/master/GithubBrowserSample) instead.
*   A real production app with network access, user authentication, etc. Check out the [Google I/O app](https://github.com/google/iosched), [Santa Tracker](https://github.com/google/santa-tracker-android) or [Tivi](https://github.com/chrisbanes/tivi) for that.

## Who is it for?

*   Intermediate developers and beginners looking for a way to structure their app in a testable and maintainable way.
*   Advanced developers looking for quick reference.

## Opening a sample in Android Studio

To open one of the samples in Android Studio, begin by checking out one of the sample branches, and then open the root directory in Android Studio. The following series of steps illustrate how to open the [usecases](tree/usecases/) sample.

Clone the repository:

```
git clone git@github.com:googlesamples/android-architecture.git
```
This step checks out the master branch. If you want to change to a different sample: 

```
git checkout usecases
```

**Note:** To review a different sample, replace `usecases` with the name of sample you want to check out.

Finally open the `android-architecture/` directory in Android Studio.

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
