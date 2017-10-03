# TODO-MVVM-RXJAVA

Project owner: [Florina Muntenescu](https://github.com/florina-muntenescu)

### Summary

// TODO

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

Similar with TODO-MVP-RxJava

### Code metrics

Compared to TODO-MVP-RxJava,

```
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Java                            49           1110           1413           3740 (3450 in MVP)
XML                             34             97            337            601
-------------------------------------------------------------------------------
SUM:                            83           1207           1750           4341

```
### Maintainability

#### Ease of amending or adding a feature

High.

#### Learning cost

Medium as RxJava is not trivial.
