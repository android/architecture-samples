package com.example.android.architecture.blueprints.todoapp.data.source;

import android.content.Context;
import android.support.v4.content.CursorLoader;

import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;

public class TaskCursorLoader extends CursorLoader {

    //  HNewsContract.StoryEntry.FILTER + " = ?",
    //new String[]{Story.FILTER.ask.name()},

    public TaskCursorLoader(Context context, long id) {
        super(context, TasksPersistenceContract.TaskEntry.buildTasksUriWith(id), TasksPersistenceContract.TaskEntry.TASKS_COLUMNS, null, null, null);
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
