package com.example.android.architecture.blueprints.todoapp.data;

import android.database.Cursor;
import android.database.MatrixCursor;

import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;

public class TaskCursor {

    /**
     * Use this constructor to return a Cursor from a Task
     *
     * @return
     */
    public static MatrixCursor from(Task task) {
        MatrixCursor taskCursor = new MatrixCursor(TasksPersistenceContract.TaskEntry.TASKS_COLUMNS);
        taskCursor.addRow(new Object[]{task.getId(), task.getTitle(), task.getDescription(), task.isCompleted()});
        return taskCursor;
    }

    /**
     * Use this constructor to return a Cursor from a Task
     *
     * @return
     */
    public static Task from(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndexOrThrow(TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION));
        boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED)) == 1;
        return new Task(title, description, id, completed);
    }

}
