# todo-mvp-contentproviders

This version of the app is called todo-mvp-contentproviders. It is based on the [todo-mvp-loaders](https://github.com/googlesamples/android-architecture/tree/todo-mvp-loaders) sample, and adds a [content provider]((https://developer.android.com/guide/topics/providers/content-providers.html)) to manage access to the underlying SQLite database storage. Content providers provide a standard interface for allowing access to data in one process from code running in a different process. This sample demonstrates how to add a content provider to the [todo-mvp-loaders](https://github.com/googlesamples/android-architecture/tree/todo-mvp-loaders) sample. 

You could then use content providers to support additional features that are not covered by this sample, providing the following possible benefits:

 * Allow you to securely share data stored in your app with other apps.
 * Add support for custom searches in your app.
 * Develop widgets which access data in your app.

Content providers can also be used to manage access to other types of storage including non-relational databases, or data stored on a file system. You can also use the [`FileProvider`](https://developer.android.com/reference/android/support/v4/content/FileProvider.html) subclass of [`ContentProvider`](https://developer.android.com/reference/android/content/ContentProvider.html) to facilitate secure sharing of files associated with an app. A [`FileProvider`](https://developer.android.com/reference/android/support/v4/content/FileProvider.html) can be used to create a `content://` URI for a file instead of a `file:///` URI. As with other versions of the [todo-mvp](https://github.com/googlesamples/android-architecture/tree/todo-mvp) app, this sample uses a SQLite database to provide data storage.

## What you need

Before exploring this sample, make sure you familiarize yourself with the following topics:

 * The [project README](https://github.com/googlesamples/android-architecture/tree/master)
 * The [todo-mvp](https://github.com/googlesamples/android-architecture/tree/todo-mvp) sample
 * The [todo-mvp-loaders](https://github.com/googlesamples/android-architecture/tree/todo-mvp-loaders) sample

In addition to the dependencies used in the todo-mvp sample, this version of the app relies on the use of [content providers](https://developer.android.com/guide/topics/providers/content-providers.html).

## Designing the app

todo-mvp-contentproviders includes two new classes in addition to those used in the todo-mvp sample:

 * [`LoaderProvider`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/LoaderProvider.java) - Creates instances of [`CursorLoader`](https://developer.android.com/reference/android/content/CursorLoader.html).
 * [`TasksProvider`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksProvider.java) - A subclass of [`ContentProvider`](https://developer.android.com/reference/android/content/ContentProvider.html).
 
In addition a number of existing classes have been modified to support the use of a content provider. For example, each of the following presenter classes in this version of the app implements the [`LoaderManager.LoaderCallbacks`](https://developer.android.com/reference/android/app/LoaderManager.LoaderCallbacks.html) interface to add the methods required to interact with the manager:

 * [`TasksPresenter`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksPresenter.java)
 * [`TaskDetailPresenter`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/taskdetail/TaskDetailPresenter.java)
 * [`StatisticsPresenter`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/statistics/StatisticsPresenter.java)
 * [`AddEditTaskPresenter`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/addedittask/AddEditTaskPresenter.java)

For more information on reviewing the changes to this version of the app, see [How to compare samples](https://github.com/googlesamples/android-architecture/wiki/How-to-compare-samples).

## Implementing the app

A [`CursorLoader`](https://developer.android.com/reference/android/content/CursorLoader.html) is used to fetch and deliver data to a presenter. The [`LoaderProvider`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/LoaderProvider.java) class is responsible for creating the [`CursorLoader`](https://developer.android.com/reference/android/content/CursorLoader.html):

```java
  return new CursorLoader(
                 mContext,
                 TasksPersistenceContract.TaskEntry.buildTasksUri(),
                 TasksPersistenceContract.TaskEntry.TASKS_COLUMNS, selection, selectionArgs, null
         );
```

The [`CursorLoader`](https://developer.android.com/reference/android/content/CursorLoader.html) then loads data from storage using a [`ContentProvider`](https://developer.android.com/reference/android/content/ContentProvider.html) which is implemented in this app as the [`TasksProvider`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksProvider.java) class. This interaction is illustrated in the following diagram:

<img src="https://github.com/googlesamples/android-architecture/wiki/images/mvp-contentproviders.png" alt="Illustrates the introduction of a content provider in this version of the app."/>

Any data that has been requested is returned to the UI thread and handled by the presenter. The following example illustrates how the [`TasksPresenter`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksPresenter.java) handles data returned by a [`CursorLoader`](https://developer.android.com/reference/android/content/CursorLoader.html).

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

The [`TasksRepository`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksRepository.java) class in this version of the app is designed to behave a little differently from the other samples in this project. The main difference is that the [`TasksRepository`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksRepository.java) class does not return data to the presenter, but instead stores the tasks in the [`LocalDataSource`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/local/TasksLocalDataSource.java). A [`CursorLoader`](https://developer.android.com/reference/android/content/CursorLoader.html) automatically registers a [`ContentObserver`](https://developer.android.com/reference/android/database/ContentObserver.html) to reload data when changes are detected. A [`ContentProvider`](https://developer.android.com/reference/android/content/ContentProvider.html) can then notify observers of changes to the URI queried as illustrated in the following delete method from [`TasksProvider`](https://github.com/googlesamples/android-architecture/blob/todo-mvp-contentproviders/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/data/source/TasksProvider.java).

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

The use of the loaders and content providers introduces additional complexity in writing unit tests for the new functionality. This sample relies on the use of tests which need to be run on either a device or emulator.

## Maintaining the app

To add or amend features to this version of the app, you should be familiar with the implementation details for loaders and content providers, both of which are in depth topics.

Note that some aspects of the code in this version are simpler as loaders take care of asynchronous requests. The following table summarizes the amount of code required for this version of the app and provides a comparison to the todo-mvp base sample.

| Language      | Number of files | Blank lines | Comment lines | Lines of code |
| ------------- | --------------- | ----------- | ------------- | ------------- |
| **Java**      |               48|         1100|           1460|           3535 (3450 in todo-mvp)|
| **XML**       |               34|           97|            337|            601|
| **Total**     |               82|         1197|           1797|           4136|


## Comparing this sample

The following summary reviews how this solution compares to the todo-mvp base sample:

 * <b>Use of architectural frameworks, libraries, or tools: </b>Loaders and content providers.
 * <b>UI testing: </b>Identical to todo-mvp.
 * <b>Ease of amending or adding a feature: </b>Similar effort to todo-mvp.
 * <b>Learning effort required: </b>Requires more background learning compared to todo-mvp.
