# Android Architecture Blueprints [beta]

<img src="https://github.com/googlesamples/android-architecture/wiki/images/aab-logo.png" alt="Android Architecture Blueprints"/>

The Android framework offers a lot of flexibility when it comes to defining how
to organize and <em>architect</em> an Android app. This freedom, whilst very valuable, can also result in apps
with large classes, inconsistent naming and architectures (or lack of) that can
make testing, maintaining and extending difficult.

Android Architecture Blueprints is meant to demonstrate possible ways to help
with these common problems. In this project we offer the same application
implemented using different architectural concepts and tools.

You can use these samples as a reference or as a starting point for creating
your own apps. The focus here is on code structure, architecture, testing and
maintainability. However, bear in mind that there are many ways to build apps
with these architectures and tools, depending on your priorities, so these
shouldn't be considered canonical examples. The UI is deliberately kept simple.

## Samples

All projects are released in their own branch. Check each project's README for
more information.

### Stable samples

  * [todo-mvp/](https://github.com/googlesamples/android-architecture/tree/todo-mvp/) - Basic Model-View-Presenter architecture.
  * [todo-mvp-loaders/](https://github.com/googlesamples/android-architecture/tree/todo-mvp-loaders/) - Based on todo-mvp, fetches data using Loaders.
  * [todo-databinding/](https://github.com/googlesamples/android-architecture/tree/todo-databinding/) - Based on todo-mvp, uses the Data Binding Library.
  * [todo-mvp-clean/](https://github.com/googlesamples/android-architecture/tree/todo-mvp-clean/) - Based on todo-mvp, uses concepts from Clean Architecture.
  * [todo-mvp-dagger/](https://github.com/googlesamples/android-architecture/tree/todo-mvp-dagger/) - Based on todo-mvp, uses Dagger2 for Dependency Injection
  * [todo-mvp-contentproviders/](https://github.com/googlesamples/android-architecture/tree/todo-mvp-contentproviders/) - Based on todo-mvp-loaders, fetches data using Loaders and uses Content Providers
  * [todo-mvp-rxjava/](https://github.com/googlesamples/android-architecture/tree/todo-mvp-rxjava/) - Based on todo-mvp, uses RxJava for concurrency and data layer abstraction.

### Samples in progress
  * [dev-todo-mvp-tablet/](https://github.com/googlesamples/android-architecture/tree/dev-todo-mvp-tablet/) - Based on todo-mvp, adds a master/detail view for tablets.

Also, see ["New sample" issues](https://github.com/googlesamples/android-architecture/issues?q=is%3Aissue+is%3Aopen+label%3A%22New+sample%22) for planned samples.

### External samples
[External samples](https://github.com/googlesamples/android-architecture/wiki/External-samples) are variants that may not be in sync with the rest of the branches.
 * [todo-mvp-fragmentless/](https://github.com/Syhids/android-architecture/tree/todo-mvp-fragmentless) - Based on todo-mvp, uses Android views instead of Fragments.
 * [todo-mvp-conductor/](https://github.com/gregpearce/android-architecture/tree/todo-mvp-conductor) - Based on todo-mvp, uses the Conductor framework to refactor to a single Activity architecture.

### What does <em>beta</em> mean?

We're still making decisions that could affect all samples so we're keeping the
initial number of variants low before the stable release.

## Why a to-do application?

The aim of the app is to be simple enough that it's understood quickly, but
complex enough to showcase difficult design decisions and testing scenarios.
Check out the [app's specification](https://github.com/googlesamples/android-architecture/wiki/To-do-app-specification).

<img src="https://github.com/googlesamples/android-architecture/wiki/images/tasks2.png" alt="Screenshot" width="160" style="display: inline; float: right"/>

Also, a similar project exists to compare JavaScript frameworks, called [TodoMVC](https://github.com/tastejs/todomvc).

## Which sample should I choose for my app?

That's for you to decide: each sample has a README where you'll find metrics
and subjective assessments. Your mileage may vary depending on the size of the
app, the size and experience of your team, the amount of maintenance that you
foresee, whether you need a tablet layout or support multiple platforms, how
compact you like your codebase, etc.

See also:
* [Samples at a glance](https://github.com/googlesamples/android-architecture/wiki/Samples-at-a-glance)
* [How to compare samples](https://github.com/googlesamples/android-architecture/wiki/How-to-compare-samples)

## Opening a sample in Android Studio

First check out one of the sample branches (`master` won't compile), and then choose to open the `todoapp/` directory. Example:

  * `git clone git@github.com:googlesamples/android-architecture.git`
  * `git checkout todo-mvp` (or replace `todo-mvp` with the project you want to check out)
  * In Android Studio open the `todoapp/` directory.

## Who is behind this project?

This project is **built by the community** and curated by Google and core maintainers.

### External contributors

[David González](http://github.com/malmstein) - Core developer (Content Providers sample)

[Karumi](http://github.com/Karumi) - Developers (MVP Clean architecture sample)

[Natalie Masse](http://github.com/freewheelnat) - Core developer

[Erik Hellman](https://github.com/ErikHellman) - Developer (MVP RxJava sample)

[Saúl Molinero](https://github.com/saulmm) - Developer (MVP Dagger sample)

[Florina Muntenescu](https://github.com/florina-muntenescu) - Developer (MVP RxJava sample)

### Googlers

[Jose Alcérreca](http://github.com/JoseAlcerreca) - Lead/Core developer

[Stephan Linzner](http://github.com/slinzner) - Core developer

[Mustafa Kurtuldu](https://github.com/mustafa-x) - UX/design

Want to be part of it? Read [how to become a contributor](https://github.com/googlesamples/android-architecture/blob/master/CONTRIBUTING.md) and the [contributor's guide](https://github.com/googlesamples/android-architecture/wiki/Contributions)
