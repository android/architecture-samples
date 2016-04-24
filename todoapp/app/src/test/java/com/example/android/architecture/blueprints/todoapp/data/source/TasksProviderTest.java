package com.example.android.architecture.blueprints.todoapp.data.source;

import android.content.Context;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TasksRemoteDataSource;
import com.google.common.collect.Lists;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class TasksProviderTest {

    @Mock
    Context context;

    @Mock
    TasksRemoteDataSource mTasksRemoteDataSource;

    @Mock
    TasksLocalDataSource mTasksLocalDataSource;

    private static List<Task> REMOTE_TASKS = Lists.newArrayList(
            new Task("Title1", "Description1"),
            new Task("Title2", "Description2")
    );

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getCache_isEmptyOnStart(){
        MockTasksProvider tasksProvider = new MockTasksProvider();

        assertTrue(true);
    }




}
