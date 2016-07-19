# Android Architecture Blueprints [beta] - MVP + Clean Architecture + Dagger2

Project owner: AndroidClasses ([funyoung](http://github.com/AndroidClasses))

### Summary
This sample stands on the principles of [Clean Architecture](https://blog.8thlight.com/uncle-bob/2012/08/13/the-clean-architecture.html).

It apply dependency injection with [Dagger2](http://google.github.io/dagger/) for [MVP Clean sample](https://github.com/googlesamples/android-architecture/tree/todo-mvp-clean), which is based on the [MVP sample](https://github.com/googlesamples/android-architecture/tree/todo-mvp), adding a domain layer between the presentation layer and repositories, splitting the app in three layers:

<img src="https://github.com/googlesamples/android-architecture/wiki/images/mvp-clean.png" alt="Diagram"/>

* **MVP**: Model View Presenter pattern from the base sample.
* **Domain**: Holds all business logic. The domain layer starts with classes named *use cases* or *interactors* used by the application presenters. These *use cases* represent all the possible actions a developer can perform from the presentation layer.
* **Repository**: Repository pattern from the base sample.  

### Key concepts
The big difference with base MVP sample is the use of the Domain layer and *use cases*. Moving the domain layer from the presenters will help to avoid code repetition on presenters (e.g. [Task filters](https://github.com/googlesamples/android-architecture/tree/todo-mvp-clean/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/domain/filter)).

*Use cases* define the operations that the app needs. This increases readability since the names of the classes make the purpose obvious (see [tasks/domain/usecase/](https://github.com/googlesamples/android-architecture/tree/todo-mvp-clean/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/domain/usecase)).

*Use cases* are good for operation reuse over our domain code. [`CompleteTask`] (https://github.com/googlesamples/android-architecture/blob/todo-mvp-clean/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/domain/usecase/CompleteTask.java) is a good example of this as it's used from both the [`TaskDetailPresenter`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-clean/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/taskdetail/TaskDetailPresenter.java) and the [`TasksPresenter`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-clean/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksPresenter.java).

The execution of these *use cases* is done in a background thread using the [command pattern](http://www.oodesign.com/command-pattern.html). The domain layer is completely decoupled from the Android SDK or other third party libraries.


[Dagger2](http://google.github.io/dagger/) is a fully static, compile-time dependency injection framework for both Java and Android. It is an adaptation of an earlier version created by Square and now maintained by Google.

Dependency injection frameworks take charge of object creation. For example, in todo-mvp we create the TasksPresenter in [TasksActivity](https://github.com/googlesamples/android-architecture/blob/todo-mvp/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksActivity.java#L75):

```java
mTasksPresenter = new TasksPresenter(
        Injection.provideTasksRepository(getApplicationContext()), tasksFragment);
```

But in this sample, the presenter is injected. Dagger2 takes care of creation and figuring out the dependencies:

```java
public class TasksActivity extends AppCompatActivity {
    @Inject TasksPresenter mTasksPresenter;
    ...
}
```

For this to work we added new classes to the feature, check out:
 * [TasksComponent](https://github.com/googlesamples/android-architecture/blob/todo-mvp-dagger/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksComponent.java)
 * [TasksPresenterModule](https://github.com/googlesamples/android-architecture/blob/todo-mvp-dagger/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksPresenterModule.java)
 * [TasksRepositoryComponent](https://github.com/googlesamples/android-architecture/blob/todo-mvp-dagger/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksRepositoryComponent.java)
 * [TasksRepositoryModule](https://github.com/googlesamples/android-architecture/blob/todo-mvp-dagger/todoapp/app/src/prod/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksRepositoryModule.java)
 * [ApplicationModule](https://github.com/googlesamples/android-architecture/blob/todo-mvp-dagger/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/ApplicationModule.java)

That's a lot of work to replace a constructor call! The main advantage of doing this is that we can substitute modules for testing. It can be done at compile time, using flavors, or at runtime, using some kind of debug panel for manual testing. They can also be configured from automated tests, to test different scenarios.

This sample is still using the mock/prod flavors from todo-mvp to generate different APKs. There is a [TasksRepositoryModule in prod/](https://github.com/googlesamples/android-architecture/blob/todo-mvp-dagger/todoapp/app/src/prod/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksRepositoryModule.java) which fetches data from the slow data source (simulating a backend API) and [another one in mock/](https://github.com/googlesamples/android-architecture/blob/todo-mvp-dagger/todoapp/app/src/mock/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksRepositoryModule.java), which provides fake data, ideal for automated testing. With this approach the app can be configured to use fake location coordinates, write and read from fake shared preferences, simulate network conditions, etc.

### Issues/notes
*Use cases* run off the main thread, which is a good solution for Android apps.  This is done as soon as possible to avoid blocking the UI thread. We decided to use a command pattern and execute each use case with a thread pool, but we can implement the same with RxJava or Promises.

We are using asynchronous repositories, but there's no need to do this any more because use cases execute off the main thread. This is kept to maintain the sample as similar as possible to the original one.

We recommend using different models for View, domain and API layers, but in this case all models are immutable so there's no need to duplicate them. If View models contained any Android-related fields, we would use two models, one for domain and other for View and a mapper class that converts between them.

Callbacks have an `onError` method that in a real app should contain information about the problem.

### Testability

Very high. With this approach, all domain code is tested with unit tests. This can be extended with integration tests, that cover from Use Cases to the boundaries of the view and repository. And the use of Dagger2 improves flexibility in local integration tests and UI tests. Components can be replaced by doubles very easily and test different scenarios.

### Dependencies

 * Dagger2
 * Clean Architecture

## Features

### Complexity - understandability

#### Use of architectural frameworks/libraries/tools:
Dagger2 and Clean Architecture pattern

#### Conceptual complexity 

Medium, it's an MVP approach with a new layer that handles domain logic. Building an app with a dependency injection framework is not trivial as it uses new concepts and many operations are done under the hood. However, once in place, it's not hard to understand and use.

### Code metrics


Adding a domain layer produces more classes and Java code.

```
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Java                            79           1405           1940           4348 (3451 in MVP)
XML                             35             97            337            602
-------------------------------------------------------------------------------
SUM:                            114           1502           2277           4950
-------------------------------------------------------------------------------
```
### Maintainability

#### Ease of amending or adding a feature
Very easy. This approach is more verbose, making the maintenance tasks more obvious.

#### Learning cost
Medium. Developers need to be aware of how Dagger2 works, although the setup of new features should look very similar to existing ones.
