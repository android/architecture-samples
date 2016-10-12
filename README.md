# TODO-DataBinding-RxJava

It is based on the [todo-mvp](https://github.com/googlesamples/android-architecture/tree/todo-mvp/todoapp) sample and combined the [todo-datatbinding](https://github.com/googlesamples/android-architecture/tree/todo-databinding/) and the [todo-mvp-rxjava](https://github.com/googlesamples/android-architecture/tree/todo-mvp-rxjava/).
Using the Data Binding library to display data and bind UI elements to actions
and through the RxJava to communication between the data model and presenter layer.

It doesn't follow a strict Model-View-ViewModel or a Model-View-Presenter
pattern, as it uses both View Models and Presenters.


## Feature components

There are multiple ways to create the relevant parts of a feature using the
Data Binding Library. In this case, the responsibility of each component in
this sample is:

  * Activity: object creation
  * Fragment: interaction with framework components (options menu, Snackbar, FAB,
Adapter for list…)
  * Presenter: receives user actions and retrieves the data from the repository. If
it doesn't do data loading, it's calling an action handler (See [TasksItemActionHandler](https://github.com/googlesamples/android-architecture/blob/todo-databinding/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksItemActionHandler.java))
  * ViewModel: Exposes data for a particular view

Some features don't have a ViewModel ([TaskDetail](https://github.com/googlesamples/android-architecture/blob/todo-databinding/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/taskdetail), [AddEditTask](https://github.com/googlesamples/android-architecture/blob/todo-databinding/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/addedittask)) as they use the Task model directly.

## Additional dependencies

* [Data Binding Library](https://developer.android.com/topic/libraries/data-binding/index.html)
* [RxJava](https://github.com/ReactiveX/RxJava)
* [RxAndroid](https://github.com/ReactiveX/RxAndroid)
* [SqlBrite](https://github.com/square/sqlbrite)


## Features

### Testability

#### Unit testing

As the Data Binding Library takes care of many of the wiring that would usually
be unit tested, the number of unit tests is lower although the  test coverage
should be similar.

#### UI testing

No difference with MVP.

### Code metrics

Compared to MVP, there are more Java classes but less code per class. Because
some wiring is moved to layouts, there are more XML lines.


```
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Java                            50           1079           1552           3327 (3450 in MVP)
XML                             34            122            337            714
-------------------------------------------------------------------------------
SUM:                            84           1201           1889           4041
-------------------------------------------------------------------------------
```
### Maintainability

#### Ease of amending or adding a feature

Easier than MVP for small changes. A new feature might require some experience
with the library.

#### Learning cost

The Data Binding library takes care of the communication between some
components, so developers need to understand what it does and doesn't before
making changes to the code.
