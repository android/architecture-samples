# TODO-MVVM-DataBinding

It is based on the [todo-databinding](https://github.com/googlesamples/android-architecture/tree/todo-databinding/) sample that uses the [Data Binding Library](http://developer.android.com/tools/data-binding/guide.html#data_objects) to display data and bind UI elements to actions.

This sample uses an MVVM (Model-View-ViewModel) architecture:

<img src="https://github.com/googlesamples/android-architecture/wiki/images/mvvm-databinding.png" alt="Diagram"/>

<pre>"View" refers to the architectural concept, not to `Android.view.View.`.</pre>

The ViewModel in MVVM is very similar to the Presenter in MVP. The main difference has to do with the communication between View and ViewModel/Presenter:
 - MVVM: when the ViewModel is modified, the View is updated by a library or framework. There's no way to update the View directly from the ViewModel, as it doesn't have this reference.
 - MVP: The Presenter has a reference to the view so when a change is neccessary, the presenter calls the view to explicitely make the change.

Observable fields in the ViewModel are *bound* to the specific TextViews, ImageViews, etc. via the layout files. The Data Binding library takes care of keeping View and ViewModel in sync. Bindings work in both ways: user actions automatically update the observable fields.

TODO: Add snackbar limitation, pros and cons, features, etc.
