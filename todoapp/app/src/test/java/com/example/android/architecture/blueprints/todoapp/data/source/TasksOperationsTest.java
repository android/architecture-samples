package com.example.android.architecture.blueprints.todoapp.data.source;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TasksOperationsTest {

    private TasksOperations mTasksOperations;

    @Mock
    private LoaderProvider mLoaderProvider;

    @Mock
    private LoaderManager mLoaderManager;

    @Mock
    private ContentResolver mContentResolver;

    @Mock
    private ContentValues mContentValues;

    @Mock
    private Bundle mBundle;

    @Mock
    private Loader<Cursor> mLoader;

    @Captor
    private ArgumentCaptor<TasksOperations.GetTasksCallback> mTaskCallbackCaptor;

    private final static String TITLE = "title";

    private final static String TITLE2 = "title2";

    private final static String TITLE3 = "title3";

    @Before
    public void setUp() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);
        mTasksOperations = new TasksOperations(mLoaderProvider, mLoaderManager, mContentResolver);
    }

    @After
    public void destroyRepositoryInstance() {
        TasksRepository.destroyInstance();
    }

    @Test
    public void getTasks_firstStartsLoader() {
        mTasksOperations.getTasks(mBundle, mTaskCallbackCaptor.capture());
        when(mLoaderManager.getLoader(anyInt())).thenReturn(null);

        verify(mLoaderManager).initLoader(any(Integer.class), any(Bundle.class), any(LoaderManager.LoaderCallbacks.class));
    }

    @Test
    public void getTasks_restartsLoaderIfLoaderExists() {
        mTasksOperations.getTasks(mBundle, mTaskCallbackCaptor.capture());
        when(mLoaderManager.getLoader(anyInt())).thenReturn(null);

        verify(mLoaderManager).restartLoader(any(Integer.class), any(Bundle.class), any(LoaderManager.LoaderCallbacks.class));

    }

    @Test
    public void completeTask_sendsArgumentsToContentResolver() {
        // Given a new task
        final Task newTask = new Task(TITLE, "");
        mTasksOperations.completeTask(newTask);

        String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {newTask.getId()};

        verify(mContentResolver).update(TasksPersistenceContract.TaskEntry.buildTasksUri(), mContentValues, selection, selectionArgs);
    }

}
