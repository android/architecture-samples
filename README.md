# Android Architecture Blueprints - Hilt
<p align="center">
<img src="https://github.com/googlesamples/android-architecture/wiki/images/aab-logov2.png" alt="Illustration by Virginia Poltrack"/>
</p>

### Summary

This sample is written in Kotlin and based on the
[master](https://github.com/googlesamples/android-architecture/tree/master) branch which uses
the following Architecture Components:
 - ViewModel
 - LiveData
 - Data Binding
 - Navigation
 - Room

It uses [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
for dependency injection.


### Differences with master

 - The ServiceLocator class is removed. Object creation and scoping is handled by Hilt.
 - Flavors `mock` and `prod` are no longer needed for testing so they're removed.


### Testing

UI tests don't rely on using the `mock` flavor to run quickly and hermetically. Instead, they
use Hilt to provide their test versions.

This is done by creating a `CustomTestRunner` that uses an `Application` configured with Hilt. As
per the [Hilt testing documentation](https://developer.android.com/training/dependency-injection/hilt-android),
`@HiltAndroidTest` will automatically create the right Hilt components for tests.


### License

```
Copyright (C) 2020 The Android Open Source Project

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
