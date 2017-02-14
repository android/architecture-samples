package com.example.android.architecture.blueprints.todoapp.tasks;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;
import com.example.android.architecture.blueprints.todoapp.util.providers.BaseNavigationProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Test class for {@link TasksNavigator}
 */
public class TasksNavigatorTest {

    @Mock
    private BaseNavigationProvider mNavigationProvider;

    private TasksNavigator mTasksNavigator;

    @Before
    public void setUp() throws Exception {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mTasksNavigator = new TasksNavigator(mNavigationProvider);
    }

    @Test
    public void addNewTask() {
        // When adding a new task
        mTasksNavigator.addNewTask();

        // The AddEditTaskActivity is opened with the correct request code
        verify(mNavigationProvider).startActivityForResult(eq(AddEditTaskActivity.class),
                eq(AddEditTaskActivity.REQUEST_ADD_TASK));
    }

    @Test
    public void openTaskDetails() {
        // Given a task id
        String taskId = "id";

        // When opening the task details
        mTasksNavigator.openTaskDetails(taskId);

        // The AddEditTaskActivity is opened with the correct request code
        verify(mNavigationProvider).startActivityForResultWithExtra(eq(TaskDetailActivity.class),
                eq(-1), eq(TaskDetailActivity.EXTRA_TASK_ID), eq(taskId));
    }
}