package com.example.android.architecture.blueprints.todoapp.data.source.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.architecture.blueprints.todoapp.data.Task
import java.util.UUID

@Entity(
    tableName = "tasks"
)
data class TaskEntity (
    // TODO: Can these be `val` without a lot of test refactoring?
    @PrimaryKey @ColumnInfo(name = "entryid") var id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var description: String = "",
    @ColumnInfo(name = "completed") var isCompleted: Boolean = false,
)

fun TaskEntity.asExternalModel() : Task {
    return Task(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
    )
}