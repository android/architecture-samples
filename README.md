# todo-mvp-loaders

This version of the app is called todo-mvp-loaders, and uses the [Loaders API](https://developer.android.com/guide/components/loaders.html) to load data from the tasks repository. The Loaders API provides the following advantages to this sample:

 * Loaders load data asynchronously, helping to free up the main thread and improving UI responsiveness.
 * Loaders are used to implement an observer which monitors the repository data source for changes, and refreshes results in the UI.
 * Loaders automatically persist query results when changes occur in configuration, such as when a device is rotated.

## What you need

Before exploring this sample, you might find it useful to familiarize yourself with the following topics:

 * The [project README](https://github.com/googlesamples/android-architecture/tree/master)
 * The [todo-mvp](https://github.com/googlesamples/android-architecture/tree/todo-mvp) sample

In addition to the dependencies used in the todo-mvp sample, this version of the app requires the [Loaders API](https://developer.android.com/guide/components/loaders.html).

## Designing the app

todo-mvp-loaders introduces two new classes that are responsible for loading data on behalf of the presenter classes from the repository, using the Loaders API:

 * [`TaskLoader`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-loaders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TaskLoader.java) - Loads data relating to a single task.
 * [`TasksLoader`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-loaders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksLoader.java) - Loads data relating to multiple tasks.
 
The two new classes check to see if previously cached results are available from the repository and returns those when possible. The following diagram illustrates how the new loaders fetch data from the repository and return it to the presenter layer:

<img src="https://github.com/googlesamples/android-architecture/wiki/images/mvp-loaders.png" alt="Illustrates the introduction of loaders in this version of the app."/>

For more information on reviewing the changes to this version of the app, see [How to compare samples](https://github.com/googlesamples/android-architecture/wiki/How-to-compare-samples).

## Implementing the app

The loader classes, [`TaskLoader`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-loaders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TaskLoader.java) and [`TasksLoader`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-loaders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksLoader.java), are responsible for fetching data, and extend [`AsyncTaskLoader`](https://developer.android.com/reference/android/content/AsyncTaskLoader.html). Results load in the background in the [`TasksLoader`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-loaders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksLoader.java) class:

```java
@Override
public List<Task> loadInBackground() {
    return mRepository.getTasks();
}
```

The UI thread receives the results, and the [`TasksPresenter`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-loaders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksPresenter.java) class handles them:

```java
@Override
public void onLoadFinished(Loader<List<Task>> loader, List<Task>
data) {
    mTasksView.setLoadingIndicator(false);

    mCurrentTasks = data;
    if (mCurrentTasks == null) {
        mTasksView.showLoadingTasksError();
    } else {
        showFilteredTasks();
    }
}
```

[`TasksPresenter`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-loaders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksPresenter.java) also loads data using the [`LoaderManager`](https://developer.android.com/reference/android/app/LoaderManager.html) class:

```java
@Override
public void start() {
    mLoaderManager.initLoader(TASKS_QUERY, null, this);
}
```

## Adding a content observer

When content changes in the data repository, [`TasksRepository`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-loaders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksRepository.java) calls `notifyContentObserver()`:

```java
@Override
public void deleteTask(@NonNull String taskId) {
    mTasksRemoteDataSource.deleteTask(checkNotNull(taskId));
    mTasksLocalDataSource.deleteTask(checkNotNull(taskId));

    mCachedTasks.remove(taskId);

    // Update the UI
   notifyContentObserver();
}
```

The `notifyContentObserver()` method then notifies [`TasksLoader`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-loaders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksLoader.java) to reload data:

```java
@Override
public void onTasksChanged() {
    if (isStarted()) {
        forceLoad();
    }
}
```

## Maintaining the app

Adding or amending features to this version of the app requires a similar amount of effort to the todo-mvp sample. Before modifying the sample, you should be familiar with the [Loaders API](https://developer.android.com/guide/components/loaders.html), which is not a trivial topic.

This sample introduces two new classes, in addition to those found in todo-mvp: [`TaskLoader`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-loaders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TaskLoader.java), and [`TasksLoader`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-loaders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksLoader.java). Some aspects of the code are simpler as loaders take care of asynchronous work. The table below summarizes the amount of code used to implement this version of the app. You can compare it with similar tables provided for each of the other samples in this project.


| Language      | Number of files | Blank lines | Comment lines | Lines of code |
| ------------- | --------------- | ----------- | ------------- | ------------- |
| **Java**      |               48|         1085|           1444|           3571 (3450 in todo-mvP)|
| **XML**       |               34|           97|            337|            601|
| **Total**     |               82|         1182|           1781|           4118|


## Comparing this sample

The following summary reviews how this solution compares to the todo-mvp base sample:

 * <b>Use of architectural frameworks, libraries, or tools: </b>Loaders API.
 * <b>UI testing: </b>Identical to todo-mvp.
 * <b>Ease of amending or adding a feature: </b>Similar effort to todo-mvp.
 * <b>Learning effort required: </b>Requires more background learning compared to todo-mvp.
