# TODO-MVP-TABLET

### Summary

This sample is based on the TODO-MVP branch, adding a list-detail view for tablets.

There are two new important classes:

 - `TasksTabletPresenter` acts as the presenter for the list-detail view. It communicates with the list and detail views through the list and detail presenters:

<img src="https://github.com/googlesamples/android-architecture/wiki/images/mvp-tablet.png" alt="Diagram"/>

The entry point for the list-detail view is `TasksActivity`. On a phone it just shows the list of tasks and opens a new `TaskDetailActivity` when a task is clicked on. On a tablet the `TaskDetailActivity` is never used.

 - `TasksMvpController` is in charge of the creation of the MVP Views and Presenters, depending on whether the device is in phone or tablet mode.

```

    private void initTasksView() {
        if (isTablet(mFragmentActivity)) {
            createTabletElements();
        } else {
            createPhoneElements();
        }
    }

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
                mTasksPresenter);

        tasksFragment.setPresenter(mTasksTabletPresenter);
        taskDetailFragment.setPresenter(mTasksTabletPresenter);
        mTasksTabletPresenter.setTaskDetailPresenter(taskDetailPresenter);
    }

    private void createPhoneElements() {
        TasksFragment tasksFragment = findOrCreateTasksFragment(R.id.contentFrame);
        mTasksPresenter = createListPresenter(tasksFragment);
        tasksFragment.setPresenter(mTasksPresenter);
    }

```
### Design decisions

The main problem this design solves is communication between fragments. It would be easy to have a
reference from one fragment to the other but it would pollute the view logic with
checks (null and phone/tablet) or abandon the idea of reusing code.

With this design, the `TasksMvpController` decides which layout to show, phone or tablet.
The `TasksTabletPresenter` handles communication between views through their respective presenters.
No changes to the fragments or the previous presenters are needed.

If a new feature is to be implemented, adding it to one of the contracts will force the
developer to write the phone and the tablet behaviors. Otherwise, a compilation error occurs.

## Features

### Complexity - understandability

#### Use of architectural frameworks/libraries/tools:

None

#### Conceptual complexity

Having three presenters at the same time can look convoluted but it prevents many runtime exceptions, 
keeping the code reusable and with clear responsibilities.

