# todo-mvvm-databinding

This version of the app is called todo-mvvm-databinding, and is based on the [todo-databinding](https://github.com/googlesamples/android-architecture/tree/deprecated-todo-databinding) sample, which uses the [Data Binding Library](http://developer.android.com/tools/data-binding/guide.html#data_objects) to display data and bind UI elements to actions.

The sample demonstrates an alternative implementation of the todo-mvp sample using the [Model-View-ViewModel](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel) (MVVM) architecture. 

## What you need

Before exploring this sample, you should familiarize yourself with the following topics:

* The [project README](https://github.com/googlesamples/android-architecture/tree/master)
* The [todo-mvp](https://github.com/googlesamples/android-architecture/tree/todo-mvp) sample
* The [todo-databinding](https://github.com/googlesamples/android-architecture/tree/deprecated-todo-databinding) sample
* The [MVVM](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel) architecture

## Designing the app

The ViewModel in the MVVM architecture plays a similar role to the Presenter in the MVP architecture. The two architectures differ in the way that the View communicates with the ViewModel or Presenter respectively: 
* When the app modifies the ViewModel in the MVVM architecture, the View is automatically updated by a library or framework. You can’t update the View directly from the ViewModel, as the ViewModel doesn't have access to the necessary reference.
* You can however update the View from the Presenter in an MVP architecture as it has the necessary reference to the View. When a change is necessary, you can explicitly call the View from the Presenter to update it.
In this project, you use layout files to bind observable fields in the ViewModel to specific UI elements such as a [TextView](https://developer.android.com/reference/android/widget/TextView.html), or [ImageView](https://developer.android.com/reference/android/widget/ImageView.html). The Data Binding Library ensures that the View and ViewModel remain in sync bi-directionally as illustrated by the following diagram.
<img src="https://github.com/googlesamples/android-architecture/wiki/images/mvvm-databinding.png" alt="Data binding keeps the View and ViewModel in sync."/>

The todo-mvvm-databinding sample includes a relatively large number of new classes, as well as many changes to existing classes. For more information on reviewing the changes to this version of the application, see [How to compare samples](https://github.com/googlesamples/android-architecture/wiki/How-to-compare-samples).
## Implementing the app
In the MVVM architecture, Views react to changes in the ViewModel without being explicitly called. However, the MVVM architecture presents some challenges when working with some Android components. 

For example, to show a [`Snackbar`](https://developer.android.com/reference/android/support/design/widget/Snackbar.html), you must use a static call to pass a view object:

```java
Snackbar.make(View coordinatorLayout, String text, int length).show();
```

When making use of a Presenter in an MVP architecture, you may call the activity or fragment to delegate responsibility for finding the appropriate view object:

```java
mView.showSnackbar(text)
```

A ViewModel however, doesn’t have the necessary reference to the activity or fragment. Instead, you can manually subscribe the snackbar to an observable field by making the following changes:
* Creating an `ObservableField<String>` in the ViewModel.
* Establishing a subscription that shows a snackbar when the `ObservableField` changes.

The following code snippet illustrates setting up a subscription between an observable field and a callback which triggers the call to show the snackbar:

```java
mViewModel.snackbarText.addOnPropertyChangedCallback(
        new Observable.OnPropertyChangedCallback() {
             @Override
             public void onPropertyChanged(Observable observable, int i) {
                 showSnackBar();
             }
         });

```

## Maintaining the app

You may find it easier to make relatively small changes to this version of the app than todo-mvp. To add new features, you may require some experience with the Data Binding Library. As the Data Binding Library takes care of most of the wiring that you would usually unit test, the number of unit tests in this version is lower. However, the overall test coverage should be similar across both versions. 

The Data Binding Library takes care of the communication between some components, so you must be familiar with its capabilities before making changes to the existing code.

The table below summarizes the amount of code used to implement this version of the app. You can use it as a basis for comparison with similar tables provided for each of the other samples in this project.

| Language      | Number of files | Blank lines | Lines of code |
| ------------- | --------------- | ----------- | ------------- |
| **Java**      |    56           |      1627   |       4093 (3901 in todo-mvp) |
| **XML**       |             35  |      352    |      751      |
| **Total**     |        91       |   1979      |     4844      |

## Comparing this sample

The following summary reviews how this solution compares to the todo-mvp base sample:

* <b>Use of architectural frameworks, libraries, or tools: </b>Developers must be familiar with the Data Binding Library.
* <b>UI testing: </b>Identical to todo-mvp
* <b>Ease of amending or adding a feature: </b>Similar effort to todo-mvp
* <b>Learning effort required: </b>This version requires more background learning compared to todo-mvp. You must be familiar with the MVVM architecture, which is conceptually similar to MVP but harder to implement. 





