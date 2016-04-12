package com.example.android.architecture.blueprints.todoapp.data.source;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

public class TasksOperationsTest {

    private TasksOperations mTasksOperations;
    private MockCursorProvider.TaskMockCursor mAllTasksCursor;

    @Mock
    private LoaderProvider mLoaderProvider;

    @Mock
    private LoaderManager mLoaderManager;

    @Mock
    private Bundle mBundle;

    @Captor
    private ArgumentCaptor<TasksOperations.GetTasksCallback> mTaskCallbackCaptor;

    @Before
    public void setUp() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);
        mTasksOperations = new TasksOperations(mLoaderProvider, mLoaderManager);
        mAllTasksCursor = MockCursorProvider.createAllTasksCursor();
    }

    @After
    public void destroyRepositoryInstance() {
        TasksRepository.destroyInstance();
    }

    @Test
    public void getTasks_firststartsLoader() {
        mTasksOperations.getTasks(mBundle, mTaskCallbackCaptor.capture());

        verify(mLoaderManager).initLoader(any(Integer.class), any(Bundle.class), any(LoaderManager.LoaderCallbacks.class));
        mTasksOperations.onLoadFinished(any(Loader.class), mAllTasksCursor);
    }

}
