package com.example.android.architecture.blueprints.todoapp.taskdetail;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment;
import com.example.android.architecture.blueprints.todoapp.util.providers.BaseNavigator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Test class for {@link TaskDetailNavigator}
 */
public class TaskDetailNavigatorTest {
    @Mock
    private BaseNavigator mNavigationProvider;

    private TaskDetailNavigator mNavigator;

    @Before
    public void setUp() throws Exception {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mNavigator = new TaskDetailNavigator(mNavigationProvider);
    }

    @Test
    public void onTaskDeleted_finishesActivity() throws Exception {
        // When a task is deleted
        mNavigator.onTaskDeleted();

        // The activity finishes
        verify(mNavigationProvider).finishActivity();
    }

    @Test
    public void onStartEditTask_startsActivity() throws Exception {
        // Given a task id
        String taskId = "id";

        // When starting to edit the task
        mNavigator.onStartEditTask(taskId);

        // The AddEditTaskActivity is opened with the correct request code
        verify(mNavigationProvider).startActivityForResultWithExtra(eq(AddEditTaskActivity.class),
                eq(TaskDetailActivity.REQUEST_EDIT_TASK),
                eq(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID), eq(taskId));
    }

    @Test
    public void onTaskEdited_finishesActivity() throws Exception {
        // When a task was edited
        mNavigator.onTaskEdited();

        // The activity finishes
        verify(mNavigationProvider).finishActivity();
    }

}