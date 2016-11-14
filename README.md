# TODO-MVP-TABLET

### Summary

This sample is based on the TODO-MVP branch, adding a list-detail view for tablets.

#### Tablet mode
<img src="https://github.com/googlesamples/android-architecture/wiki/images/mvp-tablet-tablet.png" alt="Diagram for tablet"/>

There are two new important classes:

 - `TasksTabletPresenter` acts as the presenter for every view. It communicates with the list, detail and edit views through their respective presenters.

 - `TasksMvpTabletController` is in charge of the creation of the MVP views and presenters for tablet. It also handles navigation.
 
The entry point for the list-detail view is `TasksActivity`. On a tablet, `TaskDetailActivity` and `AddEditActivity` are never used. 

#### Phone mode
<img src="https://github.com/googlesamples/android-architecture/wiki/images/mvp-tablet-phone.png" alt="Diagram for phone"/>

On a phone, `TasksTabletPresenter` and `TasksMvpTabletController` are not used but each feature uses the same view and presenter as in tablet mode.

#### TasksActivity

This activity is used in phone mode to show a list of tasks and in tablet mode to show three different MVP views. The key place where the fork happens is in `onCreate()`:

```
        // Create MVP elements for tablet or phone
        if (ActivityUtils.isTablet(this)) {
            mTasksMvpTabletController = TasksMvpTabletController.createTasksView(this);
        } else {
            createPhoneElements();
        }
```

#### TasksMvpTabletController

This class will create and store all the MVP elements for tablet mode. It also provides navigation control to show the add/edit screen, which in tablet mode is a dialog, not a full-screen fragment.

```
    private void createTabletElements() {
        // Fragment 1: List
        TasksFragment tasksFragment = findOrCreateTasksFragment(R.id.contentFrame_list);
        mTasksPresenter = createListPresenter(tasksFragment);

        // Fragment 2: Detail
        TaskDetailFragment taskDetailFragment = findOrCreateTaskDetailFragmentForTablet();
        TaskDetailPresenter taskDetailPresenter = createTaskDetailPresenter(taskDetailFragment);

        // Fragments connect to their presenters through a tablet presenter:
        mTasksTabletPresenter = new TasksTabletPresenter(
                Injection.provideTasksRepository(mFragmentActivity),
                mTasksPresenter,
                this);

        tasksFragment.setPresenter(mTasksTabletPresenter);
        taskDetailFragment.setPresenter(mTasksTabletPresenter);
        mTasksTabletPresenter.setTaskDetailPresenter(taskDetailPresenter);
    }

```
### Design decisions

The main problem to solve is communication between fragments. It would be easy to have a
reference from one fragment to the other but it would pollute the view logic with
checks (null and phone/tablet) or abandon the idea of reusing code.

With this design, the `TasksTabletPresenter` handles communication between views through their respective presenters.
No changes to the fragments or the previous presenters are needed.

If a new feature is to be implemented, adding it to one of the contracts will force the
developer to write the phone and the tablet behaviors. Otherwise, a compilation error occurs.

### Complexity - understandability

#### Use of architectural frameworks/libraries/tools:

None

#### Conceptual complexity

Having three presenters at the same time can look convoluted but it prevents many runtime exceptions, 
keeping the code reusable and with clear responsibilities.

