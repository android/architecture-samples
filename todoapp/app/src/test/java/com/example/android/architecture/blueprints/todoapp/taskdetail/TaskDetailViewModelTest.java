package com.example.android.architecture.blueprints.todoapp.taskdetail;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.providers.BaseResourceProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link TaskDetailViewModel}
 */
public class TaskDetailViewModelTest {

    private static final Task TASK = new Task("TITLE", "DESCRIPTION");
    private static final String ACTIVE = "task active";
    private static final String COMPLETED = "task completed";

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private BaseResourceProvider mResourceProvider;

    private TestSubscriber<Void> mTestSubscriber;

    private TestSubscriber<Task> mTaskTestSubscriber;

    private TestSubscriber<String> mSnackbarTestSubscriber;

    private TaskDetailViewModel mViewModel;

    @Before
    public void setup() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Define what should be returned when resources are requested
        when(mResourceProvider.getString(R.string.task_marked_complete)).thenReturn(COMPLETED);
        when(mResourceProvider.getString(R.string.task_marked_active)).thenReturn(ACTIVE);

        mTestSubscriber = new TestSubscriber<>();
        mTaskTestSubscriber = new TestSubscriber<>();
        mSnackbarTestSubscriber = new TestSubscriber<>();
    }

    @Test
    public void getLoadingIndicator_initiallyEmitsFalse() {
        // Get a reference to the class under test
        mViewModel = new TaskDetailViewModel(null, mTasksRepository, mResourceProvider);

        //When subscribing to the loading indicator
        TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();
        mViewModel.getLoadingIndicator().subscribe(testSubscriber);

        // Emits false, since the loading is not in progress
        testSubscriber.assertValue(false);
    }

    @Test
    public void getTask_withInvalidTaskId() {
        // Get a reference to the class under test with an invalid task id
        mViewModel = new TaskDetailViewModel(null, mTasksRepository, mResourceProvider);

        // When subscribing to the task
        mViewModel.getTask().subscribe(mTaskTestSubscriber);

        // An error is emitted
        mTaskTestSubscriber.assertError(Exception.class);
    }

    @Test
    public void getTask_returnsCorrectData() {
        // Given a task in the repository
        when(mTasksRepository.getTask(TASK.getId())).thenReturn(Observable.just(TASK));
        // Get a reference to the class under test for the same task id
        mViewModel = new TaskDetailViewModel(TASK.getId(), mTasksRepository, mResourceProvider);

        // When subscribing to the task
        mViewModel.getTask().subscribe(mTaskTestSubscriber);

        // The correct task is emitted
        mTaskTestSubscriber.assertValue(TASK);
    }

    @Test
    public void getTask_setsLoadingIndicatorStates() {
        // Given a task in the repository
        when(mTasksRepository.getTask(TASK.getId())).thenReturn(Observable.just(TASK));
        // Get a reference to the class under test for the same task id
        mViewModel = new TaskDetailViewModel(TASK.getId(), mTasksRepository, mResourceProvider);

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
        mViewModel = new TaskDetailViewModel(null, mTasksRepository, mResourceProvider);

        // When subscribing to the editing of the task
        TestSubscriber<String> testSubscriber = new TestSubscriber<>();
        mViewModel.editTask().subscribe(testSubscriber);

        // An error is emitted
        testSubscriber.assertError(RuntimeException.class);
    }

    @Test
    public void editTask_returnsCorrectData() {
        // Get a reference to the class under test for a task id
        mViewModel = new TaskDetailViewModel(TASK.getId(), mTasksRepository, mResourceProvider);

        // When subscribing to the editing of the task
        TestSubscriber<String> testSubscriber = new TestSubscriber<>();
        mViewModel.editTask().subscribe(testSubscriber);

        // The correct task id is emitted
        testSubscriber.assertValue(TASK.getId());
    }

    @Test
    public void deleteTask_withInvalidTaskId() {
        // Get a reference to the class under test with an invalid task id
        mViewModel = new TaskDetailViewModel(null, mTasksRepository, mResourceProvider);

        // When subscribing to the deletion of the task
        mViewModel.deleteTask().subscribe(mTestSubscriber);

        // An error is emitted
        mTestSubscriber.assertError(RuntimeException.class);
    }

    @Test
    public void deleteTask_deletesTask() {
        // Get a reference to the class under test for a task id
        mViewModel = new TaskDetailViewModel(TASK.getId(), mTasksRepository, mResourceProvider);

        // When subscribing to the deletion of the task
        mViewModel.deleteTask().subscribe(mTestSubscriber);

        // The task is deleted in the repository
        verify(mTasksRepository).deleteTask(eq(TASK.getId()));
        // The stream completes
        mTestSubscriber.assertCompleted();
    }

    @Test
    public void taskCheckChanged_withInvalidTaskId() {
        // Get a reference to the class under test with an invalid task id
        mViewModel = new TaskDetailViewModel(null, mTasksRepository, mResourceProvider);

        // When subscribing to the task changed observer
        mViewModel.taskCheckChanged(true).subscribe(mTestSubscriber);

        // An error is emitted
        mTestSubscriber.assertError(RuntimeException.class);
    }

    @Test
    public void taskCheckChanged_true_completesTask() {
        // Get a reference to the class under test for a task id
        mViewModel = new TaskDetailViewModel(TASK.getId(), mTasksRepository, mResourceProvider);

        // When subscribing to the task changed observer
        mViewModel.taskCheckChanged(true).subscribe(mTestSubscriber);

        // The task is completed in the repository
        verify(mTasksRepository).completeTask(eq(TASK.getId()));
        // The stream completes
        mTestSubscriber.assertCompleted();
    }

    @Test
    public void taskCheckChanged_false_activatesTask() {
        // Get a reference to the class under test for a task id
        mViewModel = new TaskDetailViewModel(TASK.getId(), mTasksRepository, mResourceProvider);

        // When subscribing to the task changed observer
        mViewModel.taskCheckChanged(false).subscribe(mTestSubscriber);

        // The task is activated in the repository
        verify(mTasksRepository).activateTask(eq(TASK.getId()));
        // The stream completes
        mTestSubscriber.assertCompleted();
    }

    @Test
    public void taskCheckChanged_true_completesTask_showsSnackbarText() {
        // Get a reference to the class under test for a task id
        mViewModel = new TaskDetailViewModel(TASK.getId(), mTasksRepository, mResourceProvider);

        // When subscribed to the snackbar text emissions
        mViewModel.getSnackbarText().subscribe(mSnackbarTestSubscriber);
        // and a task is marked as completed
        mViewModel.taskCheckChanged(true).subscribe();

        // A "Completed" snackbar text is emitted
        mSnackbarTestSubscriber.assertValue(COMPLETED);
    }

    @Test
    public void taskCheckChanged_false_activatesTask_showsSnackbarText() {
        // Get a reference to the class under test for a task id
        mViewModel = new TaskDetailViewModel(TASK.getId(), mTasksRepository, mResourceProvider);

        // When subscribed to the snackbar text emissions
        mViewModel.getSnackbarText().subscribe(mSnackbarTestSubscriber);
        // and a task is marked as active
        mViewModel.taskCheckChanged(false).subscribe();

        // An "Active" snackbar text is emitted
        mSnackbarTestSubscriber.assertValue(ACTIVE);
    }
}
