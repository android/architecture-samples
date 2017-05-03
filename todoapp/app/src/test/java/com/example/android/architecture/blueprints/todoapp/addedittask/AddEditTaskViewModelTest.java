package com.example.android.architecture.blueprints.todoapp.addedittask;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Completable;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link AddEditTaskViewModel}.
 */
public class AddEditTaskViewModelTest {

    private static final String NEW_TITLE = "new title";
    private static final String NEW_DESCRIPTION = "new description";
    private static final Task TASK = new Task("TITLE", "DESCRIPTION");

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private AddEditTaskNavigator mNavigator;

    private TestSubscriber<Task> mTaskTestSubscriber;

    private TestSubscriber<Integer> mSnackbarTestSubscriber;

    private TestSubscriber<Void> mCompletableTestSubscriber;

    private AddEditTaskViewModel mViewModel;

    @Before
    public void setUp() throws Exception {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        mTaskTestSubscriber = new TestSubscriber<>();
        mSnackbarTestSubscriber = new TestSubscriber<>();
        mCompletableTestSubscriber = new TestSubscriber<>();
    }

    @Test
    public void getTask_withNullTaskId_doesNotEmit() {
        mViewModel = new AddEditTaskViewModel(null, mTasksRepository, mNavigator);

        mViewModel.getTask().subscribe(mTaskTestSubscriber);

        mTaskTestSubscriber.assertNoValues();
    }

    @Test
    public void getTask_withEmptyTaskId_doesNotEmit() {
        mViewModel = new AddEditTaskViewModel("", mTasksRepository, mNavigator);

        mViewModel.getTask().subscribe(mTaskTestSubscriber);

        mTaskTestSubscriber.assertNoValues();
    }

    @Test
    public void getTask_returnsCorrectData() {
        // Given a task in the repository
        when(mTasksRepository.getTask(TASK.getId())).thenReturn(Observable.just(TASK));
        // Get a reference to the class under test for a task id
        mViewModel = new AddEditTaskViewModel(TASK.getId(), mTasksRepository, mNavigator);

        // When subscribing to the task
        mViewModel.getTask().subscribe(mTaskTestSubscriber);

        // The correct task is emitted
        mTaskTestSubscriber.assertValue(TASK);
    }

    @Test
    public void getTask_whenRepositoryReturnsError_snackbarEmits() {
        // Given a task in the repository
        when(mTasksRepository.getTask(TASK.getId())).thenReturn(Observable.error(new Exception()));
        // Get a reference to the class under test for a task id
        mViewModel = new AddEditTaskViewModel(TASK.getId(), mTasksRepository, mNavigator);
        // With subscribed to the snackbar
        mViewModel.getSnackbarText().subscribe(mSnackbarTestSubscriber);

        // When subscribing to the task
        mViewModel.getTask().subscribe(mTaskTestSubscriber);

        // The correct resource id is emitted
        mSnackbarTestSubscriber.assertValue(R.string.empty_task_message);
    }

    @Test
    public void saveTask_emitsError_whenErrorSavingInRepository() {
        // Get a reference to the class under test for a null task id
        mViewModel = new AddEditTaskViewModel(null, mTasksRepository, mNavigator);
        // Given that the repository returns an error when a task is saved
        when(mTasksRepository.saveTask(any(Task.class)))
                .thenReturn(Completable.error(new RuntimeException()));

        // When saving a task
        mViewModel.saveTask(NEW_TITLE, NEW_DESCRIPTION).subscribe(mCompletableTestSubscriber);

        // An error is emitted
        mCompletableTestSubscriber.assertError(RuntimeException.class);
    }

    @Test
    public void saveTask_whenNoTask_createsTask() {
        // Get a reference to the class under test for a null task id
        mViewModel = new AddEditTaskViewModel(null, mTasksRepository, mNavigator);
        withTaskInRepositorySavedSuccessfully();

        // When saving a task
        mViewModel.saveTask(NEW_TITLE, NEW_DESCRIPTION).subscribe();

        // The task is saved in repository
        verify(mTasksRepository).saveTask(any(Task.class));
    }

    @Test
    public void saveTask_whenNoTask_navigates() {
        // Get a reference to the class under test for a null task id
        mViewModel = new AddEditTaskViewModel(null, mTasksRepository, mNavigator);
        withTaskInRepositorySavedSuccessfully();

        // When saving a task
        mViewModel.saveTask(NEW_TITLE, NEW_DESCRIPTION).subscribe();

        // Navigation is triggered
        verify(mNavigator).onTaskSaved();
    }

    @Test
    public void saveTask_withEmptyTitleAndDescription_doesntSaveTask() {
        // Get a reference to the class under test for a null task id
        mViewModel = new AddEditTaskViewModel(null, mTasksRepository, mNavigator);

        // When saving an invalid task
        mViewModel.saveTask("", "");

        // The invalid task is not saved in repository
        verify(mTasksRepository, never()).saveTask(any(Task.class));
    }

    @Test
    public void saveTask_withEmptyTitleAndDescription_triggersSnackbar() {
        // Get a reference to the class under test for a null task id
        mViewModel = new AddEditTaskViewModel(null, mTasksRepository, mNavigator);
        // With subscribed to snackbar text
        mViewModel.getSnackbarText().subscribe(mSnackbarTestSubscriber);

        // When saving an invalid task
        mViewModel.saveTask("", "").subscribe();

        // The snackbar text emits with empty message
        mSnackbarTestSubscriber.assertValue(R.string.empty_task_message);
    }

    @Test
    public void saveTask_withExistingTaskId_savesTask() {
        // Get a reference to the class under test for a task id
        mViewModel = new AddEditTaskViewModel(TASK.getId(), mTasksRepository, mNavigator);
        withTaskInRepositorySavedSuccessfully();

        // When saving the task with new title and new description
        mViewModel.saveTask(NEW_TITLE, NEW_DESCRIPTION).subscribe();

        // The updated task is saved in the repository
        Task expected = new Task(NEW_TITLE, NEW_DESCRIPTION, TASK.getId());
        verify(mTasksRepository).saveTask(expected);
    }

    @Test
    public void saveTask_withExistingTaskId_navigates() {
        // Get a reference to the class under test for a task id
        mViewModel = new AddEditTaskViewModel(TASK.getId(), mTasksRepository, mNavigator);
        withTaskInRepositorySavedSuccessfully();

        // When saving the task with new title and new description
        mViewModel.saveTask(NEW_TITLE, NEW_DESCRIPTION).subscribe(mCompletableTestSubscriber);

        // The navigation is triggered
        verify(mNavigator).onTaskSaved();
        // The completable completes
        mCompletableTestSubscriber.assertCompleted();
    }

    @Test
    public void restoreTask_withTitleUpdated() {
        // Given a task in the repository
        when(mTasksRepository.getTask(TASK.getId())).thenReturn(Observable.just(TASK));
        // Get a reference to the class under test for a task id
        mViewModel = new AddEditTaskViewModel(TASK.getId(), mTasksRepository, mNavigator);

        // When setting a restore title
        mViewModel.setRestoredState(NEW_TITLE, null);
        // When that we are subscribing to the task
        mViewModel.getTask().subscribe(mTaskTestSubscriber);

        // The emitted task has the restored title
        Task task = mTaskTestSubscriber.getOnNextEvents().get(0);
        assertEquals(task.getTitle(), NEW_TITLE);
        // And all the initial values of the task
        assertEquals(task.getId(), TASK.getId());
        assertEquals(task.getDescription(), TASK.getDescription());
    }

    @Test
    public void restoreTask_withDescriptionUpdated() {
        // Given a task in the repository
        when(mTasksRepository.getTask(TASK.getId())).thenReturn(Observable.just(TASK));
        // Get a reference to the class under test for a task id
        mViewModel = new AddEditTaskViewModel(TASK.getId(), mTasksRepository, mNavigator);

        // When setting a restore description
        mViewModel.setRestoredState(null, NEW_DESCRIPTION);
        // When that we are subscribing to the task
        mViewModel.getTask().subscribe(mTaskTestSubscriber);

        // The emitted task has the restored description
        Task task = mTaskTestSubscriber.getOnNextEvents().get(0);
        assertEquals(task.getDescription(), NEW_DESCRIPTION);
        // And all the initial values of the task
        assertEquals(task.getId(), TASK.getId());
        assertEquals(task.getTitle(), TASK.getTitle());
    }

    private void withTaskInRepositorySavedSuccessfully() {
        when(mTasksRepository.saveTask(any(Task.class))).thenReturn(Completable.complete());
    }
}
