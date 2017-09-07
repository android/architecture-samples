# todo-mvvm-live-kotlin

This version of the app is called todo-mvvm-live-kotlin, and it is the kotlin version of [todo-mvvm-live](https://github.com/googlesamples/android-architecture/tree/todo-mvvm-live/).
It uses some Architecture Components like ViewModel, LiveData, and other lifecycle-aware classes.

This sample is not final, as the Architecture Components are in alpha stage at the time of writing this document.

## What you need

Before exploring this sample, you should familiarize yourself with the following topics:

* The [project README](https://github.com/googlesamples/android-architecture/tree/master)
* The [todo-mvp](https://github.com/googlesamples/android-architecture/tree/todo-mvp) sample
* The [todo-mvvm-databinding](https://github.com/googlesamples/android-architecture/tree/todo-mvvm-databinding) sample
* The [MVVM](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel) architecture
* The [Kotlin programming language](https://kotlinlang.org)

## Implementing the app

Although the parent sample already used ViewModels, as it was following an MVVM architecture, the Architecture
Components have different restrictions by design.

In the MVVM architecture, Views react to changes in the ViewModel without being explicitly called. However, the MVVM
architecture presents some challenges when working with some Android components.

### Live events

A new `SingleLiveEvent` class is created, which extends `MutableLiveData` so it's lifecycle-aware. It's used for
communication between ViewModels and UI views (activities and fragments).

Instead of holding data, it dispatches data once. This is important to prevent events being fired after a rotation, for
example.

A convenient use for this is navigation. There is no reference to the View from a ViewModel so the communication between
them must happen via a subscription. ViewModels expose events like `openTaskEvent` and views subscribe to them. For
example:

```kotlin
    private fun subscribeToNavigationChanges(viewModel: TaskDetailViewModel) {
        // The activity observes the navigation commands in the ViewModel
        viewModel.run {
            editTaskCommand.observe(this@TaskDetailActivity,
                    Observer { this@TaskDetailActivity.onStartEditTask() })
            deleteTaskCommand.observe(this@TaskDetailActivity,
                    Observer { this@TaskDetailActivity.onTaskDeleted() })
        }
    }
```

### Snackbar

To show a [`Snackbar`](https://developer.android.com/reference/android/support/design/widget/Snackbar.html), you must
use a static call to pass a view object. In this kotlin example, we wrap it in an extension function for View defined
as follows:

```kotlin
fun View.showSnackbar(snackbarText: String) {
    Snackbar.make(this, snackbarText, Snackbar.LENGTH_LONG).show()
}
```

A ViewModel, however, doesn't have the necessary reference to the view hierarchy to call this function. Instead, you can
manually subscribe the snackbar to a Snackbar event. In this case the subscription is made to a `SingleLiveEvent<Int>`
and takes a string resource ID (hence the Int parameter type). There's only one snackbar and there should only be one
active observer at a time. Messages are only shown once.

### TasksAdapter
There is no `TaskItemViewModel` in this branch for each particular item in the list, so tasks in the list only
communicate with the list's ViewModel.

### Using ViewModels in bindings with the Data Binding Library
ViewModels are used to show data of a particular screen, but they don't handle user actions. For that it's much more
convenient to create user actions listeners or even presenters that hold no state during configuration changes and hence
are easy to recreate. See `TaskItemUserActionsListener` for an example.

### Repository does not use LiveData
For simplicity and similarity with the parent branch, the repository does not use LiveData to expose its data.

### Code Metrics

```
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Kotlin                          58            898           1755           3228 (3060 in MVP-kotlin)
XML                             22            127              0            974
-------------------------------------------------------------------------------
SUM:                            80           1015           1755           4192
-------------------------------------------------------------------------------
```
