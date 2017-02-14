package com.example.android.architecture.blueprints.todoapp.taskdetail;

import android.app.Activity;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.providers.BaseNavigationProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link TaskDetailViewModel}
 */
public class TaskDetailViewModelTest {

    private static final Task TASK_WITH_TITLE_DESCRIPTION = new Task("TITLE", "DESCRIPTION");
    private static final Task TASK_WITH_TITLE_COMPLETED = new Task("TITLE", "", true);

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private BaseNavigationProvider mNavigationProvider;

    private TestSubscriber<Void> mTestSubscriber;

    private TestSubscriber<TaskModel> mTaskTestSubscriber;

    private TestSubscriber<Integer> mSnackbarTestSubscriber;

    private TaskDetailViewModel mViewModel;

    @Before
    public void setup() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        mTestSubscriber = new TestSubscriber<>();
        mTaskTestSubscriber = new TestSubscriber<>();
        mSnackbarTestSubscriber = new TestSubscriber<>();
    }

    @Test
    public void getLoadingIndicator_initiallyEmitsFalse() {
        // Get a reference to the class under test
        mViewModel = new TaskDetailViewModel(null, mTasksRepository, mNavigationProvider);

        //When subscribing to the loading indicator
        TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();
        mViewModel.getLoadingIndicator().subscribe(testSubscriber);

        // Emits false, since the loading is not in progress
        testSubscriber.assertValue(false);
    }

    @Test
    public void getTask_withInvalidTaskId() {
        // Get a reference to the class under test with an invalid task id
        mViewModel = new TaskDetailViewModel(null, mTasksRepository, mNavigationProvider);

        // When subscribing to the task
        mViewModel.getTask().subscribe(mTaskTestSubscriber);

        // An error is emitted
        mTaskTestSubscriber.assertError(Exception.class);
    }

    @Test
    public void getTask_withTitleAndDescription_returnsCorrectData() {
        // Given a task in the repository
        when(mTasksRepository.getTask(TASK_WITH_TITLE_DESCRIPTION.getId()))
                .thenReturn(Observable.just(TASK_WITH_TITLE_DESCRIPTION));
        // Get a reference to the class under test for the same task id
        mViewModel = new TaskDetailViewModel(TASK_WITH_TITLE_DESCRIPTION.getId(), mTasksRepository,
                mNavigationProvider);

        // When subscribing to the task
        mViewModel.getTask().subscribe(mTaskTestSubscriber);

        // The correct task is emitted
        TaskModel taskModel = mTaskTestSubscriber.getOnNextEvents().get(0);
        assertTaskWithTitleAndDescription(taskModel);
    }

    @Test
    public void getTask_withTitleAndCompleted_returnsCorrectData() {
        // Given a task in the repository
        when(mTasksRepository.getTask(TASK_WITH_TITLE_DESCRIPTION.getId()))
                .thenReturn(Observable.just(TASK_WITH_TITLE_COMPLETED));
        // Get a reference to the class under test for the same task id
        mViewModel = new TaskDetailViewModel(TASK_WITH_TITLE_DESCRIPTION.getId(), mTasksRepository,
                mNavigationProvider);

        // When subscribing to the task
        mViewModel.getTask().subscribe(mTaskTestSubscriber);

        // The correct task is emitted
        TaskModel taskModel = mTaskTestSubscriber.getOnNextEvents().get(0);
        assertTaskWithTitleAndCompleted(taskModel);
    }

    @Test
    public void getTask_setsLoadingIndicatorStates() {
        // Given a task in the repository
        when(mTasksRepository.getTask(TASK_WITH_TITLE_DESCRIPTION.getId())).thenReturn(Observable.just(TASK_WITH_TITLE_DESCRIPTION));
        // Get a reference to the class under test for the same task id
        mViewModel = new TaskDetailViewModel(TASK_WITH_TITLE_DESCRIPTION.getId(), mTasksRepository, mNavigationProvider);

        //When subscribing to the loading indicator updates
        TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();
        mViewModel.getLoadingIndicator().subscribe(testSubscriber);
        // When subscribing to the task
        mViewModel.getTask().subscribe();

        // The loading indicator emits initial values, then when the loading is in progress and when
        // loading is done
        testSubscriber.assertValues(false, true, false);
    }

    @Test
    public void editTask_withInvalidTaskId() {
        // Get a reference to the class under test with an invalid task id
        mViewModel = new TaskDetailViewModel(null, mTasksRepository, mNavigationProvider);

        // When subscribing to the editing of the task
        TestSubscriber<String> testSubscriber = new TestSubscriber<>();
        mViewModel.editTask().subscribe(testSubscriber);

        // An error is emitted
        testSubscriber.assertError(RuntimeException.class);
    }

    @Test
    public void editTask_returnsCorrectData() {
        // Get a reference to the class under test for a task id
        mViewModel = new TaskDetailViewModel(TASK_WITH_TITLE_DESCRIPTION.getId(), mTasksRepository, mNavigationProvider);

        // When subscribing to the editing of the task
        mViewModel.editTask().subscribe(mTestSubscriber);

        // The Completable completes
        mTestSubscriber.assertCompleted();
    }

    @Test
    public void editTask_startsActivity() {
        // Get a reference to the class under test for a task id
        mViewModel = new TaskDetailViewModel(TASK_WITH_TITLE_DESCRIPTION.getId(), mTasksRepository, mNavigationProvider);

        // When subscribing to the editing of the task
        mViewModel.editTask().subscribe(mTestSubscriber);

        // Starts activity
        verify(mNavigationProvider).startActivityForResultWithExtra(eq(AddEditTaskActivity.class),
                eq(TaskDetailActivity.REQUEST_EDIT_TASK),
                eq(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID),
                eq(TASK_WITH_TITLE_DESCRIPTION.getId()));
    }

    @Test
    public void handleActivityResult_withResultOK_finishesActivity() {
        // Get a reference to the class under test with an invalid task id
        mViewModel = new TaskDetailViewModel(null, mTasksRepository, mNavigationProvider);

        //When handling activity result for edit task
        mViewModel.handleActivityResult(TaskDetailActivity.REQUEST_EDIT_TASK, Activity.RESULT_OK);

        // Finishes activity
        verify(mNavigationProvider).finishActivity();
    }

    @Test
    public void handleActivityResult_withCancel_doesntFinishActivity() {
        // Get a reference to the class under test with an invalid task id
        mViewModel = new TaskDetailViewModel(null, mTasksRepository, mNavigationProvider);

        //When handling activity result for edit task
        mViewModel.handleActivityResult(TaskDetailActivity.REQUEST_EDIT_TASK, Activity.RESULT_CANCELED);

        // Finishes activity
        verify(mNavigationProvider, never()).finishActivity();
    }

    @Test
    public void deleteTask_withInvalidTaskId() {
        // Get a reference to the class under test with an invalid task id
        mViewModel = new TaskDetailViewModel(null, mTasksRepository, mNavigationProvider);

        // When subscribing to the deletion of the task
        mViewModel.deleteTask().subscribe(mTestSubscriber);

        // An error is emitted
        mTestSubscriber.assertError(RuntimeException.class);
    }

    @Test
    public void deleteTask_deletesTask() {
        // Get a reference to the class under test for a task id
        mViewModel = new TaskDetailViewModel(TASK_WITH_TITLE_DESCRIPTION.getId(), mTasksRepository, mNavigationProvider);

        // When subscribing to the deletion of the task
        mViewModel.deleteTask().subscribe(mTestSubscriber);

        // The task is deleted in the repository
        verify(mTasksRepository).deleteTask(eq(TASK_WITH_TITLE_DESCRIPTION.getId()));
        // The stream completes
        mTestSubscriber.assertCompleted();
    }


    @Test
    public void deleteTask_finishesActivity() {
        // Get a reference to the class under test for a task id
        mViewModel = new TaskDetailViewModel(TASK_WITH_TITLE_DESCRIPTION.getId(), mTasksRepository, mNavigationProvider);

        // When subscribing to the deletion of the task
        mViewModel.deleteTask().subscribe(mTestSubscriber);

        // The activity finishes
        mNavigationProvider.finishActivity();
    }

    @Test
    public void taskCheckChanged_withInvalidTaskId() {
        // Get a reference to the class under test with an invalid task id
        mViewModel = new TaskDetailViewModel(null, mTasksRepository, mNavigationProvider);

        // When subscribing to the task changed observer
        mViewModel.taskCheckChanged(true).subscribe(mTestSubscriber);

        // An error is emitted
        mTestSubscriber.assertError(RuntimeException.class);
    }

    @Test
    public void taskCheckChanged_true_completesTask() {
        // Get a reference to the class under test for a task id
        mViewModel = new TaskDetailViewModel(TASK_WITH_TITLE_DESCRIPTION.getId(), mTasksRepository, mNavigationProvider);

        // When subscribing to the task changed observer
        mViewModel.taskCheckChanged(true).subscribe(mTestSubscriber);

        // The task is completed in the repository
        verify(mTasksRepository).completeTask(eq(TASK_WITH_TITLE_DESCRIPTION.getId()));
        // The stream completes
        mTestSubscriber.assertCompleted();
    }

    @Test
    public void taskCheckChanged_false_activatesTask() {
        // Get a reference to the class under test for a task id
        mViewModel = new TaskDetailViewModel(TASK_WITH_TITLE_DESCRIPTION.getId(), mTasksRepository, mNavigationProvider);

        // When subscribing to the task changed observer
        mViewModel.taskCheckChanged(false).subscribe(mTestSubscriber);

        // The task is activated in the repository
        verify(mTasksRepository).activateTask(eq(TASK_WITH_TITLE_DESCRIPTION.getId()));
        // The stream completes
        mTestSubscriber.assertCompleted();
    }

    @Test
    public void taskCheckChanged_true_completesTask_showsSnackbarText() {
        // Get a reference to the class under test for a task id
        mViewModel = new TaskDetailViewModel(TASK_WITH_TITLE_DESCRIPTION.getId(), mTasksRepository, mNavigationProvider);

        // When subscribed to the snackbar text emissions
        mViewModel.getSnackbarText().subscribe(mSnackbarTestSubscriber);
        // and a task is marked as completed
        mViewModel.taskCheckChanged(true).subscribe();

        // A "Completed" snackbar text is emitted
        mSnackbarTestSubscriber.assertValue(R.string.task_marked_complete);
    }

    @Test
    public void taskCheckChanged_false_activatesTask_showsSnackbarText() {
        // Get a reference to the class under test for a task id
        mViewModel = new TaskDetailViewModel(TASK_WITH_TITLE_DESCRIPTION.getId(), mTasksRepository, mNavigationProvider);

        // When subscribed to the snackbar text emissions
        mViewModel.getSnackbarText().subscribe(mSnackbarTestSubscriber);
        // and a task is marked as active
        mViewModel.taskCheckChanged(false).subscribe();

        // An "Active" snackbar text is emitted
        mSnackbarTestSubscriber.assertValue(R.string.task_marked_active);
    }

    private void assertTaskWithTitleAndDescription(TaskModel model) {
        assertEquals(model.getTitle(), TASK_WITH_TITLE_DESCRIPTION.getTitle());
        assertTrue(model.isShowTitle());
        assertEquals(model.getDescription(), TASK_WITH_TITLE_DESCRIPTION.getDescription());
        assertTrue(model.isShowDescription());
        assertFalse(model.isChecked());
    }

    private void assertTaskWithTitleAndCompleted(TaskModel model) {
        assertEquals(model.getTitle(), TASK_WITH_TITLE_COMPLETED.getTitle());
        assertTrue(model.isShowTitle());
        assertEquals(model.getDescription(), TASK_WITH_TITLE_COMPLETED.getDescription());
        assertFalse(model.isShowDescription());
        assertTrue(model.isChecked());
    }
}
