# TODO-MVP-ContentProviders

It is based on the [TODO-MVP-Loaders](https://github.com/googlesamples/android-architecture/tree/todo-mvp-loaders) sample and uses a Content Provider to retrieve data into the repository.

<img src="https://github.com/googlesamples/android-architecture/wiki/images/mvp-contentproviders.png" alt="Diagram"/>

The advantages of Content Providers, from the [Content Provider documentation page](http://developer.android.com/guide/topics/providers/content-providers.html), are:

  * Manage access to a structured set of data
  * Content providers are the standard interface that connects data in one process with code running in another process

The advantages of Loaders, from the [Loaders documentation page](http://developer.android.com/guide/components/loaders.html), are:

  * They provide asynchronous loading of data, removing the need for callbacks in the repository.
  * They monitor the source of their data and deliver new results when the content changes, in our case, the repository.
  * They automatically reconnect to the last loader when being recreated after a configuration change.

## Code

### Asynchronous loading

The data is fetched by [Cursor Loaders](http://developer.android.com/reference/android/support/v4/content/CursorLoader.html) and delivered to the Presenters.
The Presenter does not need to know how the data is loaded, for that reason the LoaderProvider class is passed as a dependency of the Presenter and is in charge
of dealing with the data loading.


In [src/data/source/LoaderProvider.java](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/LoaderProvider.java):


```java
  return new CursorLoader(
                 mContext,
                 TasksPersistenceContract.TaskEntry.buildTasksUri(),
                 TasksPersistenceContract.TaskEntry.TASKS_COLUMNS, selection, selectionArgs, null
         );
```
The results are received in the UI Thread, handled by the Presenter.

In [TasksPresenter.java](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksPresenter.java)


```java
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            if (data.moveToLast()) {
                onDataLoaded(data);
            } else {
                onDataEmpty();
            }
        } else {
            onDataNotAvailable();
        }
    }
```

Since CursorLoaders have Observers listening to the underlying data changes in the ContentProvider, we don't need to tell them directly that new data is available.
For that reason, the Presenter tells the Repository to go and grab the latest data.

```java
   /**
     * We will always have fresh data from remote, the Loaders handle the local data
     */
    public void loadTasks() {
        mTasksView.setLoadingIndicator(true);
        mTasksRepository.getTasks(this);
    }
```

Upon start, once the data has been stored locally we simply ask the LoaderProvider to start the Loader

 ```java
     @Override
     public void onTasksLoaded() {
         if (mLoaderManager.getLoader(TASKS_LOADER) == null) {
             mLoaderManager.initLoader(TASKS_LOADER, mCurrentFiltering.getFilterExtras(), this);
         } else {
             mLoaderManager.restartLoader(TASKS_LOADER, mCurrentFiltering.getFilterExtras(), this);
         }
     }
 ```


### Content Provider and Tasks Repository

The [TasksRepository](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksRepository.java#L81) behaves a bit differently compared to the other branches. The main difference is that it's not returning data to the `Presenter`, but only storing the tasks in the [LocalDataSource](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/local/TasksLocalDataSource.java#L61). Once the `ContentProvider` inserts data, it will [notify](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksProvider.java#L128) the change to the `Uri` and whoever is observing it will receive an update.

```java
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mTasksDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case TASK:
                rowsDeleted = db.delete(
                        TasksPersistenceContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }
```

That's why there's no need to return the data to the `Presenter`, the `CursorLoader` will always have the data present in the `ContentProvider`


## Additional considerations

Content Providers are the perfect solution to share content with other applications. If your application doesn't need to share content then this is not
the best approach for you.

One can make the most out of them when using [Widgets](https://developer.android.com/design/patterns/widgets.html) or you'd like to share content the same
way [Calendar](https://developer.android.com/guide/topics/providers/calendar-provider.html) or [Contacts](https://developer.android.com/guide/topics/providers/contacts-provider.html) do.

Another benefit of Content Providers is that they don't need to use a SQLite database underneath, one could use other approach to give data to the application. For
 example, [FileProvider](https://developer.android.com/reference/android/support/v4/content/FileProvider.html) is a great example of that. FileProvider is a special subclass of `ContentProvider` that facilitates secure sharing of files associated with an app by creating a content:// Uri for a file instead of a file:/// Uri.

## Features

### Complexity - understandability

#### Use of architectural frameworks/libraries/tools:

No external frameworks.

#### Conceptual complexity

Developers need to be familiar with the Loaders and Content Providers framework, which is not
trivial. Following this approach is harder to decouple from dependencies of the Android Framework.

### Testability

#### Unit testing

The use of the Loaders framework adds a big dependency with the Android
framework so unit testing is harder. Same goes for the Content Provider, forcing us to use AndroidJUnit
tests which run in a device / emulator and not in the developer's local JVM.

#### UI testing

No difference with MVP.

### Code metrics

Compared to MVP, the only new classes are LoaderProvider and TasksProvider. Parts of
the code are simpler as Loaders take care of the asynchronous work.


```
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Java                            48           1100           1460           3535 (3450 in MVP)
XML                             34             97            337            601
-------------------------------------------------------------------------------
SUM:                            82           1197           1797           4136
-------------------------------------------------------------------------------

```

### Maintainability

#### Ease of amending or adding a feature

Similar to MVP Loaders

#### Learning cost

Medium as the Loaders and Content Providers framework are not trivial.
