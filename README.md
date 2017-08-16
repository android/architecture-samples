# Android Architecture Blueprints - MVP + Dagger2 + Dagger-Android
### Summary
This sample is based on the TODO-MVP project and uses Dagger to externalize  the creation of dependencies from the classes that use them.

### Key concepts

[Dagger2](http://google.github.io/dagger/) is a fully static, compile-time dependency injection framework for both Java and Android. It is an adaptation of an earlier version created by Square and now maintained by Google.

[Dagger-Android](https://google.github.io/dagger//android.html) are Android specific helpers for Android, specifically the auto generation of sub components using a new code generator. 

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

For this to work we added new classes to the feature, mostly within the`example/android/architecture/blueprints/todoapp/di` package, check out:
 * [AppComponent](https://github.com/googlesamples/android-architecture/blob/todo-mvp-dagger/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/di/AppComponent.java) - Here is where we register `modules` which bring in individual `providers` to our app. Notice we also bring in an auto generated `AndroidSupportInjectionModule` which comes from our `Dagger-Android` dependency.
  * [ActivityBindingModule](https://github.com/googlesamples/android-architecture/blob/todo-mvp-dagger/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/di/ActivityBindingModule.java) - This module  has a `@ContributesInjector` for each `Activity` in our app.  Dagger Android will generate a subcomponent for each one of these abstract methods:
  ```   
  @ActivityScoped
    @ContributesAndroidInjector(modules = TasksModule.class)
    abstract TasksActivity tasksActivity();
    ```
 generates a subcomponent that looks like 
 ```
 @Subcomponent(modules = TasksModule.class)
  @ActivityScoped
  public interface TasksActivitySubcomponent extends AndroidInjector<TasksActivity> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<TasksActivity> {}
  }
}
```


 * [TasksModule](https://github.com/googlesamples/android-architecture/blob/todo-mvp-dagger/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksModule.java) - provides a `Fragment` and `Presenter` for our `TaskActivity` to use.  Additionally we create a fragment subcomponent here
 ```
     @FragmentScoped
    @ContributesAndroidInjector
    abstract TasksFragment tasksFragment();
    ```

* Lastly we have our activities extend `DaggerActivity` and our application extend `DaggerApplication` which will inject (provide) our dependencies when we need them

 The main advantage of doing this is that we can swap different implementations of dependencies for testing. By externalizing all instance creation from the objects that use them it becomes trivial to pass in fake or mock dependencies during testing.  This can be done at compile time, using flavors, or at runtime, using some kind of debug panel for manual testing. Dependencies can also be configured from automated tests, to test different scenarios.  With Dependency Injection you will have a place for creating your objects rather than having to instantiate objects within your activties or application class.  

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

Medium. Developers need to be aware of how Dagger2 works, although the setup of new features should look very similar to existing ones.  Once Dagger is setup within an app it is no more difficult to add a new feature than without it.
