# todo-mvp-room
# Summary
This sample is based on the TODO-MVP project and uses [Room](https://developer.android.com/topic/libraries/architecture/room.html), the data persistence library part of the [Architecture Components](https://developer.android.com/topic/libraries/architecture/index.html).

Compared with the TODO-MVP, the UI layer stays the same. The changes are done in the implementation of the local repository, where a Room database is used.

This sample is not final, as the Architecture Components are in alpha stage at the time of writing this document.

# Dependencies
*  [Room](https://developer.android.com/topic/libraries/architecture/room.html)

## Features

### Complexity - understandability

Developers need to be familiar with Room.

### Testability

#### Unit testing

Similar to TODO-MVP.

#### Integration testing

High, since all database related classes can be tested. 

### Code metrics

Compared to TODO-MVP, a Data Access Object class and a Database class were added, while some of the classes that worked with the `SQLiteDatabase` API were removed.

```
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Java                            49           1196           1648           3832 (3450 in MVP)
XML                             34             97            338            608
-------------------------------------------------------------------------------
SUM:                            83           1293           1986           4440
-------------------------------------------------------------------------------
```
### Maintainability

#### Ease of amending or adding a feature

High.

#### Learning cost

Low, as working with Room does not involve a high learning curve
