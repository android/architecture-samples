package com.example.android.architecture.blueprints.todoapp.data.source;

import android.content.Context;
import android.support.v4.content.CursorLoader;

import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;

public class TasksCursorLoader extends CursorLoader {

    public TasksCursorLoader(Context context, long id) {
        super(context, TasksPersistenceContract.TaskEntry.buildTasksUri(), TasksPersistenceContract.TaskEntry.TASKS_SQL_COLUMNS, null, null, null);
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
