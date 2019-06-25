# Android Architecture Blueprints - AAC + Dagger2 + Dagger-Android
### Summary
This sample is written in Kotlin and based on the
[master](https://github.com/googlesamples/android-architecture/tree/master) branch which uses
the following Architecture Components:
 - ViewModel
 - LiveData
 - Data Binding
 - Navigation
 - Room

It introduces a new layer called `domain` where the Use Cases (also called Interactors) live. The 
`domain` layer is where the business logic happens, which is the code that determines what
the app _does_ with the data coming from the repository before it's exposed to the UI for
display.

The todo app is too simple to showcase a complete representation of 
[Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html), 
but it adheres to some of its rules, which work well in a modern Android application: separation
of concerns, high level of abstraction and the dependency rule, which in our case means that layers 
only know about what's directly underneath them:
 - Presentation layer knows about use cases (domain layer).
 - Domain layer knows about repository (data layer) but not the Presentation layer.
 - Data layer doesn't know about domain or presentation layers.

This allows for easier testing and maintenance and recommended for bigger projects (alongside 
modularization).

### Differences with master

 - ViewModels don't receive a repository but a set of Use Cases, which are reused throughout the
 presentation layer.
 - Business logic that was present in ViewModels is moved to Use Cases. This is important because
 ViewModels tend to grow quickly in size in real applications. 


### Key files

The only relevant use case in this example is `GetTasksUseCase`. It contains some business logic
that used to be in the ViewModel. It's decoupled from the view so it can be thoroughly unit tested
in `GetTasksUseCaseTest`.

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
