# TODO-MVP-RXJAVA

It's based on the TODO-MVP sample and uses RxJava for communication between the data model and presenter layers.

### Summary

Compared to the TODO-MVP, both the Presenter contracts and the implementation of the Views stay the same. The changes are done to the data model layer and in the implementation of the Presenters.

The data model layer exposes RxJava ``Observable`` streams as a way of retrieving tasks. The ``TasksDataSource`` interface contains methods like:

```java
Observable<List<Task>> getTasks();

Observable<Task> getTask(@NonNull String taskId);
```

This is implemented in ``TasksLocalDataSource`` with the help of [SqlBrite](https://github.com/square/sqlbrite). The result of queries to the database being easily exposed as streams of data.

```java
@Override
public Observable<List<Task>> getTasks() {
    ...
    return mDatabaseHelper.createQuery(TaskEntry.TABLE_NAME, sql)
            .mapToList(mTaskMapperFunction);
}
```

The ``TasksRepository`` combines the streams of data from the local and the remote data sources, exposing it to whoever needs it. In our project, the Presenters and the unit tests are actually the consumers of these ``Observable``s.

The Presenters subscribe to the ``Observable``s from the ``TasksRepository`` and after manipulating the data, they are the ones that decide what the views should display, in the ``.subscribe(...)`` method. Also, the Presenters are the ones that decide on the working threads. For example, in the ``StatisticsPresenter``, we decide on which thread we should do the computation of the active and completed tasks and what should happen when this computation is done: show the statistics, if all is ok; show loading statistics error, if needed; and telling the view that the loading indicator should not be visible anymore.

```java
...
Subscription subscription = Observable
        .zip(completedTasks, activeTasks, new Func2<Integer, Integer, Pair<Integer, Integer>>() {
            @Override
            public Pair<Integer, Integer> call(Integer completed, Integer active) {
                return Pair.create(active, completed);
            }
        })
        .subscribeOn(mSchedulerProvider.computation())
        .observeOn(mSchedulerProvider.ui())
        .subscribe(new Action1<Pair<Integer, Integer>>() {
            @Override
            public void call(Pair<Integer, Integer> stats) {
                mStatisticsView.showStatistics(stats.first, stats.second);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                mStatisticsView.showLoadingStatisticsError();
            }
        }, new Action0() {
            @Override
            public void call() {
                mStatisticsView.setProgressIndicator(false);
            }
        });
```

### Dependencies

* [RxJava](https://github.com/ReactiveX/RxJava)
* [RxAndroid](https://github.com/ReactiveX/RxAndroid)
* [SqlBrite](https://github.com/square/sqlbrite)

## Features

### Complexity - understandability

#### Use of architectural frameworks/libraries/tools:

Building an app with RxJava is not trivial as it uses new concepts.

#### Conceptual complexity

Developers need to be familiar with RxJava, which is not trivial.

### Testability

#### Unit testing

Very High. Given that the RxJava ``Observable``s are highly unit testable, unit tests are easy to implement.

#### UI testing

Similar with TODO-MVP

### Code metrics

Compared to TODO-MVP, new classes were added for handing the ``Schedulers`` that provide the working threads.

```
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Java                            46           1075           1451           3451
XML                             34             97            337            601
-------------------------------------------------------------------------------
SUM:                            80           1172           1788           4052
-------------------------------------------------------------------------------
```
### Maintainability

#### Ease of amending or adding a feature

High.

#### Learning cost

Medium as RxJava is not trivial.
