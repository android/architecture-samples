# Android Architecture Blueprints [beta] - Clean Architecture

### Summary
This sample is based on the principles of [Clean Architecture](https://blog.8thlight.com/uncle-bob/2012/08/13/the-clean-architecture.html).

This sample add a domain layer between the presenter layer and repositories. This split the app in three layers:

* **MVP**: Model View Presenter pattern from base project.
* **Domain**: Holding all business logic. The domain layer starts with some classes named use cases or interactors and used by the application presenters. These use cases represent all the possible actions a developer can perform with the domain layer. The execution of this Use Cases are in a background thread using the [command pattern](http://www.oodesign.com/command-pattern.html). All the domain layer is completely decoupled from the Android SDK or another third party libraries.
* **Repository**: Repository pattern from base project.  

### Key concepts
The big difference with base mvp project is the use of Domain Layer and use cases. Move domain layer from presenter will help us to avoid repeat code on presenters, for example sorting tasks features.
Use cases will help us to define the operations that our app define, this is going to be helpful for software readability, only reading use cases classes names can know what operations allow or software and it's useful for new people joining to our team, or when we come back to a old project.
Use cases are good for reuse operations over or domain code. Mark as complete a task is a good sample about this one.

### Known issues
Use cases run out of main thread, this is a good solution for Android apps, we go out from main thread as soon as possible for not blocking ui thread. We decide use a command pattern and execute each use case with an thread pool, but we can implement the same with RxJava or Promises.
We are using asynchronous repositories, but don't have sense for the sample approach, because use cases are out of main thread, we decided not change repositories to a synchronous version for maintain the sample similar to original one.
We recommend use different models from View, domain a api layers, but in this case all models are immutable and do not have sense duplicate those models. If View models contained any Android related fields, we would have two models, one for domain and other for View and a mapper class that convert for one to other.
  
### Testability

With this approach we can test all domain code with unit tests. If our business logic is complex, we can have social unit tests, that test from Use Cases to our view and repositories boundaries.



