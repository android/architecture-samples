# TODO-MVP-ContentProvider

It is based on the [TODO-MVP-Loaders](https://github.com/googlesamples/android-architecture/tree/master/todo-mvp-loaders) sample and uses a Content Provider to retrieve data to the repository.

<img src="https://github.com/googlesamples/android-architecture/wiki/images/mvp-contentproviders.png" alt="Diagram"/>

//TODO finish README

The advantages of Loaders, from the [Loaders documentation page](http://developer.android.com/guide/components/loaders.html), are:

  * They provide asynchronous loading of data, removing the need for callbacks in
the repository.
  * They monitor the source of their data and deliver new results when the content
changes, in our case, the repository.
  * They automatically reconnect to the last loader when being recreated after a
configuration change.

## Code

### Asynchronous loading

The Loaders ([TaskLoader](https://github.com/googlesamples/android-architecture/blob/todo-mvp-loaders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TaskLoader.java) and [TasksLoader](https://github.com/googlesamples/android-architecture/blob/todo-mvp-loaders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksLoader.java)) are responsible for fetching the data and extend AsyncTaskLoader.

In [src/data/source/TasksLoader.java](https://github.com/googlesamples/android-architecture/blob/todo-mvp-loaders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksLoader.java):


```
    @Override
    public List<Task> loadInBackground() {
        return mRepository.getTasks();
    }
```
The results are received in the UI Thread, handled by the presenter.

In [TasksPresenter.java](https://github.com/googlesamples/android-architecture/blob/todo-mvp-loaders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksPresenter.java)


```
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
The presenter also triggers the loading the data, like in the MVP sample but in
this case it does it through the LoaderManager:


```
    @Override
    public void start() {
        mLoaderManager.initLoader(TASKS_QUERY, null, this);
    }
```
### Content observer

After every content change in the repository, <code>notifyContentObserver()</code> is called.

In [src/data/source/TasksRepository.java](https://github.com/googlesamples/android-architecture/blob/todo-mvp-loaders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksRepository.java):


```
    @Override
    public void deleteTask(@NonNull String taskId) {
        mTasksRemoteDataSource.deleteTask(checkNotNull(taskId));
        mTasksLocalDataSource.deleteTask(checkNotNull(taskId));

        mCachedTasks.remove(taskId);

        // Update the UI
       notifyContentObserver();
    }
```
This notifies the Loader which in this case simply forces a reload of data.

In [TasksLoader.java](https://github.com/googlesamples/android-architecture/blob/todo-mvp-loaders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksLoader.java):


```
    @Override
    public void onTasksChanged() {
        if (isStarted()) {
            forceLoad();
        }
    }
```
## Additional dependencies

This project uses the Loaders framework available from Android 3.0 (API Level
11).

## Features

### Complexity - understandability

#### Use of architectural frameworks/libraries/tools: 

No external frameworks. 

#### Conceptual complexity 

Developers need to be familiar with the Loaders framework, which is not
trivial.

### Testability

#### Unit testing

The use of the Loaders framework adds a big dependency with the Android
framework so unit testing is harder.

#### UI testing

No difference with MVP.

### Code metrics

Compared to MVP, the only new classes are TaskLoader and TasksLoader. Parts of
the code are simpler as Loaders take care of the asynchronous work. 


```
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Java                            48           1085           1444           3517 (3450 in MVP)
XML                             34             97            337            601
-------------------------------------------------------------------------------
SUM:                            82           1182           1781           4118
-------------------------------------------------------------------------------

```
### Maintainability

#### Ease of amending or adding a feature

Similar to MVP

#### Learning cost

Medium as the Loaders framework is not trivial.

