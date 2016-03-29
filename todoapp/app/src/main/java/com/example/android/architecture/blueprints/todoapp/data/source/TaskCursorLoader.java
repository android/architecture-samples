package com.example.android.architecture.blueprints.todoapp.data.source;

import android.content.Context;
import android.support.v4.content.CursorLoader;

import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;

public class TaskCursorLoader extends CursorLoader {

    public TaskCursorLoader(Context context, int id) {
        super(context, TasksPersistenceContract.TaskEntry.buildTasksUri(),
              TasksPersistenceContract.TaskEntry.TASKS_COLUMNS,
              TasksPersistenceContract.TaskEntry._ID + " = ? ",
              new String[]{String.valueOf(id)}, null);
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
    }

}
