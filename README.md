# Android Architecture Blueprints [beta] - draft

The Android framework is very flexible when it comes to defining how to
organize and <em>architect</em> an Android app. This freedom comes at a price: Huge classes, inconsistent
naming patterns and architectures (or lack of) that make testing, maintaining
and extending difficult.

Android Architecture Blueprints is meant to help developers compare quickly. In
this project we offer the same to-do application implemented using different
architectural concepts and tools. Examples are MVP, MVVM, Dagger2, Data Binding
Library, RxJava, Loaders…

### What does <em>beta</em> mean?

We're still making decisions that could affect all samples so we're keeping the
initial number of variants low before the stable release. 

## Samples

Check each project's README for more information.

  * [todo-mvp/](https://github.com/googlesamples/android-architecture/tree/master/todo-mvp) - Basic Model-View-Presenter architecture
  * [todo-mvp-loaders/](https://github.com/googlesamples/android-architecture/tree/master/todo-mvp-loaders) - Based in todo-mvp, fetches data using Loaders.

In progress:

  * todo-mvp-dagger/ - Based in todo-mvp, uses the Dagger2 dependency Injection
framework.
  * todo-mvp-rxjava/ - Based in todo-mvp, uses RxJava to handle load and observe
data
  * todo-databinding/ - Uses the Data Binding Library to display data

## Why a to-do application?

Check out the <u>app's specification</u>. The aim of the app is to be simple enough that it's understood quickly, but
complex enough to showcase difficult design decisions and hard (but common)
testing scenarios. 

Also, a similar project exists to compare JavaScript frameworks, called [TodoMVC](https://github.com/tastejs/todomvc).

## Which sample should I choose for my app?

That's for you to decide: each sample has a README where you'll find metrics
and subjective assessments. Your mileage may vary depending on the size of the
app, the size and experience of your team, the amount of maintenance that you
foresee, whether you need a tablet layout or support multiple platforms, how
compact you like your codebase, etc.

## Who is behind this project?

This project is made by the community and curated by Google and core
maintainers. Each sample has a group of owners that look after it keeping it up
to date and handling issues and pull requests. 

### Contributors

Apart from the awesome [contributors](https://github.com/googlesamples/android-architecture/contributors), the core team is composed of:

  * TODO

Want to be part of it? [Read on](https://github.com/googlesamples/android-architecture/wiki/Contributions).

