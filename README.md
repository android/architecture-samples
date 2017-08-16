# Android Architecture Blueprints - MVP + Dagger2
### Summary

### Key concepts

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

### Dependencies
 
 * Dagger2

## Features

### Complexity - understandability

#### Use of architectural frameworks/libraries/tools:

Building an app with a dependency injection framework is not trivial as it uses new concepts and many operations are done under the hood. However, once in place, it's not hard to understand and use.

### Testability

Very high. Use of Dagger2 improves flexibility in local integration tests and UI tests. Components can be replaced by doubles very easily and test different scenarios.

### Code metrics
```
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Java                            58           1193           1658           3901 (3451 in MVP)
XML                             34             97            338            610
-------------------------------------------------------------------------------
SUM:                            92           1290           1996           4511
-------------------------------------------------------------------------------

```
### Maintainability

#### Ease of amending or adding a feature + Learning cost

Medium. Developers need to be aware of how Dagger2 works, although the setup of new features should look very similar to existing ones.
