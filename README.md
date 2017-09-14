# TODO-MVP-RXJAVA

Project owners: 

* [Erik Hellman](https://github.com/erikhellman)
* [Florina Muntenescu](https://github.com/florina-muntenescu)
* [Voicu Klein](https://github.com/kleinsenberg)

### Summary

This sample is based on the TODO-MVP project and uses RxJava 2 for communication between the data model and presenter layers.

Compared to the TODO-MVP, both the Presenter contracts and the implementation of the Views stay the same. The changes are done to the data model layer and in the implementation of the Presenters. For the sake of simplicity we decided to keep the RxJava usage minimal, leaving optimizations like RxJava caching aside.

The data model layer exposes RxJava 2 [Flowable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html) streams as a way of retrieving tasks. The ``TasksDataSource`` interface contains methods like:

```java
Flowable<List<Task>> getTasks();

Flowable<Optional<Task>> getTask(@NonNull String taskId);
```

A major difference between RxJava 1 and 2 is that streams no longer support the propagation of null items, so we are using Guava [Optional](https://google.github.io/guava/releases/19.0/api/docs/com/google/common/base/Optional.html).

To get a better understanding of the differences between RxJava 1 and RxJava 2 click [here](https://github.com/ReactiveX/RxJava/wiki/What%27s-different-in-2.0).

This is implemented in ``TasksLocalDataSource`` with the help of [SqlBrite](https://github.com/square/sqlbrite). The result of queries to the database being easily exposed as streams of data.

```java
@Override
public Observable<List<Task>> getTasks() {
    ...
    return mDatabaseHelper.createQuery(TaskEntry.TABLE_NAME, sql)
            .mapToList(mTaskMapperFunction);
}
```

The ``TasksRepository`` combines the streams of data from the local and the remote data sources, exposing it to whoever needs it. In our project, the Presenters and the unit tests are actually the consumers of these ``Flowable``s.

The Presenters subscribe to the ``Flowable``s from the ``TasksRepository`` and after manipulating the data, they are the ones that decide what the views should display, in the ``.subscribe(...)`` method. Also, the Presenters are the ones that decide on the working threads. For example, in the ``StatisticsPresenter``, we decide on which thread we should do the computation of the active and completed tasks and what should happen when this computation is done: show the statistics, if all is ok; show loading statistics error, if needed; and telling the view that the loading indicator should not be visible anymore.

```java
...
Disposable disposable = Flowable
    .zip(completedTasks, activeTasks, new BiFunction<Long, Long, Pair<Long, Long>>() {
        @Override
        public Pair<Long, Long> apply(Long completed, Long active) throws Exception {
            return Pair.create(active, completed);
        }
     })
     .subscribeOn(mSchedulerProvider.computation())
     .observeOn(mSchedulerProvider.ui())
     .subscribe(
         new Consumer<Pair<Long, Long>>() {
             @Override
             public void accept(Pair<Long, Long> stats) throws Exception {
                 mStatisticsView.showStatistics(stats.first, stats.second),
             }
         },
         new Consumer<Throwable>() {
             @Override
             public void accept(Throwable throwable) throws Exception {
                 mStatisticsView.showLoadingStatisticsError();
             }
         },
         new Action() {
             @Override
             public void run() throws Exception {
                 mStatisticsView.setProgressIndicator(false);
             }
     });
```

Handling of the working threads is done with the help of RxJava's `Scheduler`s. For example, the creation of the database together with all the database queries is happening on the IO thread. The `subscribeOn` and `observeOn` methods are used in the Presenter classes to define that the `Flowable`s will operate on the computation thread and that the observing is on the main thread.

### Dependencies

* [RxJava 2.x](https://github.com/ReactiveX/RxJava)
* [RxAndroid 2.x](https://github.com/ReactiveX/RxAndroid)
* [SqlBrite 2.x](https://github.com/square/sqlbrite)

### Java 8 Compatibility

This project uses [lambda expressions](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html) extensively, one of the features of [Java 8](https://developer.android.com/guide/platform/j8-jack.html). To check out how the translation to lambdas was made, check out [this commit](https://github.com/googlesamples/android-architecture/pull/240/commits/929f63e3657be8705679c46c75e2625dc44a5b28), where lambdas and the Jack compiler were enabled.

## Features

### Complexity - understandability

#### Use of architectural frameworks/libraries/tools:

Building an app with RxJava is not trivial as it uses new concepts.

#### Conceptual complexity

Developers need to be familiar with RxJava, which is not trivial.

### Testability

#### Unit testing

Very High. Given that the RxJava ``Flowable``s are highly unit testable, unit tests are easy to implement.

#### UI testing

Similar with TODO-MVP

### Code metrics

Compared to TODO-MVP, new classes were added for handing the ``Schedulers`` that provide the working threads.

```
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Java                            48           1118           1422           3639 (3450 in MVP)
XML                             35             97            337            952
-------------------------------------------------------------------------------
SUM:                            83           1215           1759           4591

```
### Maintainability

#### Ease of amending or adding a feature

High.

#### Learning cost

Medium as RxJava is not trivial.
