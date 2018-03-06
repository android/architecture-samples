package com.example.android.architecture.blueprints.todoapp.data.source.cache;

import com.example.android.architecture.blueprints.todoapp.data.Task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TasksCache {

    private static TasksCache INSTANCE;

    private Map<String, Task> mCache;

    private boolean mDirty = false;

    public static TasksCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TasksCache();
        }
        return INSTANCE;
    }

    // Prevent direct instantiation.
    private TasksCache() {}

    public boolean isDirty() {
        return mDirty;
    }

    public Task getTask(String taskId) {
        if (mCache == null) return null;
        return mCache.get(taskId);
    }

    public ArrayList<Task> getTasks() {
        if (mCache == null) return new ArrayList<>();
        return new ArrayList<>(mCache.values());
    }

    public void saveTask(Task task) {
        if (mCache == null) mCache = new LinkedHashMap<>();
        mCache.put(task.getId(), task);
    }

    public void deleteTask(String taskId) {
        if (mCache == null) mCache = new LinkedHashMap<>();
        mCache.remove(taskId);
    }

    public void deleteAllTasks() {
        if (mCache == null) mCache = new LinkedHashMap<>();
        mCache.clear();
    }

    public void clearCompletedTasks() {
        if (mCache == null) mCache = new LinkedHashMap<>();
        Iterator<Map.Entry<String, Task>> it = mCache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Task> entry = it.next();
            if (entry.getValue().isCompleted()) {
                it.remove();
            }
        }
    }

    public void invalidate() {
        mDirty = true;
    }

    public void refresh(List<Task> tasks) {
        if (mCache == null) mCache = new LinkedHashMap<>();
        mCache.clear();
        for (Task task : tasks) {
            mCache.put(task.getId(), task);
        }
        mDirty = false;
    }

}
