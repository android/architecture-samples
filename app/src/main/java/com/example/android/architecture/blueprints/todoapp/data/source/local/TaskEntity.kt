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
    @PrimaryKey @ColumnInfo(name = "entryid") val id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var description: String = "",
    @ColumnInfo(name = "completed") var isCompleted: Boolean = false,
)

// Mapping functions to and from the external model.
fun TaskEntity.asExternalModel() : Task {
    return Task(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
    )
}

fun Task.asLocalModel() : TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
    )
}
