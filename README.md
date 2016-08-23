# TODO-MVP-RXJAVA

It's based on the TODO-MVP sample and uses RxJava for communication between layers.

### Summary
TODO

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
