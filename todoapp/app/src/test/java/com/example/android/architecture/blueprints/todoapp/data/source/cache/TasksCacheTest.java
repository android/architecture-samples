package com.example.android.architecture.blueprints.todoapp.data.source.cache;

import com.example.android.architecture.blueprints.todoapp.data.Task;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TasksCacheTest {

    private final static String TASK_TITLE = "title";

    private TasksCache mTasksCache;

    @Before
    public void setUp() {
        mTasksCache = TasksCache.getInstance();
    }

    @After
    public void tearDown() {
        mTasksCache.refresh(new ArrayList<Task>());
    }

    @Test
    public void isNotDirty_initially() throws Exception {
        assertFalse(mTasksCache.isDirty());
    }

    @Test
    public void isEmpty_initially() throws Exception {
        assertEquals(mTasksCache.isEmpty(), true);
    }

    @Test
    public void isNotEmpty_afterAddingTasks() throws Exception {
        Task task = new Task(TASK_TITLE, "Some Task Description");

        mTasksCache.saveTask(task);

        assertFalse(mTasksCache.isEmpty());
    }

    @Test
    public void getTask_returnsTaskSpecified() throws Exception {
        Task task = new Task(TASK_TITLE, "Some Task Description");

        mTasksCache.saveTask(task);

        assertEquals(mTasksCache.getTask(task.getId()).getId(), task.getId());
    }

    @Test
    public void getTask_returnsNullForUnsavedTaskId() throws Exception {
        Task task = new Task(TASK_TITLE, "Some Task Description");

        mTasksCache.saveTask(task);

        assertNull(mTasksCache.getTask(task.getId() + "a"));
    }

    @Test
    public void getTasks_returnsAllTasks() throws Exception {
        Task task1 = new Task(TASK_TITLE, "Some Task Description");
        Task task2 = new Task(TASK_TITLE, "Some Task Description");

        mTasksCache.saveTask(task1);
        mTasksCache.saveTask(task2);

        List<Task> tasks = mTasksCache.getTasks();
        assertEquals(tasks.size(), 2);
    }

    @Test
    public void deleteTask_deletesTaskSpecified() throws Exception {
        Task task = new Task(TASK_TITLE, "Some Task Description");

        mTasksCache.saveTask(task);
        mTasksCache.deleteTask(task.getId());

        assertNull(mTasksCache.getTask(task.getId()));
    }

    @Test
    public void deleteTask_doesNotCrashWithIncorrectId() throws Exception {
        Task task = new Task(TASK_TITLE, "Some Task Description");

        mTasksCache.saveTask(task);
        mTasksCache.deleteTask(task.getId() + "a");
    }

    @Test
    public void deleteAllTasks_deletesAllTasks() throws Exception {
        Task task1 = new Task(TASK_TITLE, "Some Task Description");
        Task task2 = new Task(TASK_TITLE, "Some Task Description");

        mTasksCache.saveTask(task1);
        mTasksCache.saveTask(task2);
        mTasksCache.deleteAllTasks();

        List<Task> tasks = mTasksCache.getTasks();
        assertEquals(tasks.size(), 0);
    }

    @Test
    public void clearCompletedTasks_clearsCompletedTasksOnly() throws Exception {
        Task task1 = new Task(TASK_TITLE, "Some Task Description", true);
        Task task2 = new Task(TASK_TITLE, "Some Task Description");

        mTasksCache.saveTask(task1);
        mTasksCache.saveTask(task2);
        mTasksCache.clearCompletedTasks();

        List<Task> tasks = mTasksCache.getTasks();
        assertEquals(tasks.size(), 1);
        assertEquals(task2.getId(), tasks.get(0).getId());
    }

    @Test
    public void invalidate_makesCacheDirty() throws Exception {
        mTasksCache.invalidate();
        assertTrue(mTasksCache.isDirty());
    }

    @Test
    public void refresh_deletesAllTasksAndMakesCacheClean() throws Exception {
        Task task1 = new Task(TASK_TITLE, "Some Task Description");
        Task task2 = new Task(TASK_TITLE, "Some Task Description");
        Task task3 = new Task(TASK_TITLE, "Some Task Description");

        mTasksCache.saveTask(task1);
        mTasksCache.saveTask(task2);

        List<Task> tasks = new ArrayList<>();
        tasks.add(task3);
        mTasksCache.refresh(tasks);

        tasks = mTasksCache.getTasks();
        assertFalse(mTasksCache.isDirty());
        assertEquals(tasks.size(), 1);
        assertEquals(task3.getId(), tasks.get(0).getId());
    }

}