package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.app.Activity;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.providers.BaseNavigationProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.InvalidObjectException;

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

    private static final Task TASK = new Task("TITLE", "DESCRIPTION");

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private BaseNavigationProvider mNavigationProvider;

    private TestSubscriber<Task> mTaskTestSubscriber;

    private TestSubscriber<Integer> mSnackbarTestSubscriber;

    private AddEditTaskViewModel mViewModel;

    @Before
    public void setUp() throws Exception {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        mTaskTestSubscriber = new TestSubscriber<>();
        mSnackbarTestSubscriber = new TestSubscriber<>();
    }

    @Test
    public void getTask_withNullTaskId_emitsError() {
        mViewModel = new AddEditTaskViewModel(null, mTasksRepository, mNavigationProvider);

        mViewModel.getTask().subscribe(mTaskTestSubscriber);

        mTaskTestSubscriber.assertError(InvalidObjectException.class);
    }

    @Test
    public void getTask_withEmptyTaskId_emitsError() {
        mViewModel = new AddEditTaskViewModel("", mTasksRepository, mNavigationProvider);

        mViewModel.getTask().subscribe(mTaskTestSubscriber);

        mTaskTestSubscriber.assertError(InvalidObjectException.class);
    }

    @Test
    public void getTask_returnsCorrectData() {
        // Given a task in the repository
        when(mTasksRepository.getTask(TASK.getId())).thenReturn(Observable.just(TASK));
        // Get a reference to the class under test for a task id
        mViewModel = new AddEditTaskViewModel(TASK.getId(), mTasksRepository, mNavigationProvider);

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
        mViewModel = new AddEditTaskViewModel(TASK.getId(), mTasksRepository, mNavigationProvider);
        // With subscribed to the snackbar
        mViewModel.getSnackbarText().subscribe(mSnackbarTestSubscriber);

        // When subscribing to the task
        mViewModel.getTask().subscribe(mTaskTestSubscriber);

        // The correct resource id is emitted
        mSnackbarTestSubscriber.assertValue(R.string.empty_task_message);
    }

    @Test
    public void saveTask_whenNoTask_createsTask() {
        // Get a reference to the class under test for a null task id
        mViewModel = new AddEditTaskViewModel(null, mTasksRepository, mNavigationProvider);

        // When saving a task
        mViewModel.saveTask("title", "description");

        // The task is saved in repository
        verify(mTasksRepository).saveTask(any(Task.class));
    }

    @Test
    public void saveTask_whenNoTask_finishesActivity() {
        // Get a reference to the class under test for a null task id
        mViewModel = new AddEditTaskViewModel(null, mTasksRepository, mNavigationProvider);

        // When saving a task
        mViewModel.saveTask("title", "description");

        // The Activity finishes with result
        verify(mNavigationProvider).finishActivityWithResult(Activity.RESULT_OK);
    }

    @Test
    public void saveTask_withEmptyTitleAndDescription_doesntSaveTask() {
        // Get a reference to the class under test for a null task id
        mViewModel = new AddEditTaskViewModel(null, mTasksRepository, mNavigationProvider);

        // When saving an invalid task
        mViewModel.saveTask("", "");

        // The invalid task is not saved in repository
        verify(mTasksRepository, never()).saveTask(any(Task.class));
    }

    @Test
    public void saveTask_withEmptyTitleAndDescription_triggersSnackbar() {
        // Get a reference to the class under test for a null task id
        mViewModel = new AddEditTaskViewModel(null, mTasksRepository, mNavigationProvider);
        // With subscribed to snackbar text
        mViewModel.getSnackbarText().subscribe(mSnackbarTestSubscriber);

        // When saving an invalid task
        mViewModel.saveTask("", "");

        // The snackbar text emits with empty message
        mSnackbarTestSubscriber.assertValue(R.string.empty_task_message);
    }

    @Test
    public void saveTask_withExistingTaskId_savesTask() {
        // Get a reference to the class under test for a task id
        mViewModel = new AddEditTaskViewModel(TASK.getId(), mTasksRepository, mNavigationProvider);

        // When saving the task with new title and new description
        String newTitle = "new title";
        String newDescription = "new description";
        mViewModel.saveTask(newTitle, newDescription);

        // The updated task is saved in the repository
        Task expected = new Task(newTitle, newDescription, TASK.getId());
        verify(mTasksRepository).saveTask(expected);
    }

    @Test
    public void saveTask_withExistingTaskId_finishesActivity() {
        // Get a reference to the class under test for a task id
        mViewModel = new AddEditTaskViewModel(TASK.getId(), mTasksRepository, mNavigationProvider);

        // When saving the task with new title and new description
        String newTitle = "new title";
        String newDescription = "new description";
        mViewModel.saveTask(newTitle, newDescription);

        // The Activity finishes with result
        verify(mNavigationProvider).finishActivityWithResult(Activity.RESULT_OK);
    }

    @Test
    public void restoreTask_withTitleUpdated() {
        // Given a task in the repository
        when(mTasksRepository.getTask(TASK.getId())).thenReturn(Observable.just(TASK));
        // Get a reference to the class under test for a task id
        mViewModel = new AddEditTaskViewModel(TASK.getId(), mTasksRepository, mNavigationProvider);

        // When setting a restore title
        String restoredTitle = "new title";
        mViewModel.setRestoredState(restoredTitle, null);
        // When that we are subscribing to the task
        mViewModel.getTask().subscribe(mTaskTestSubscriber);

        // The emitted task has the restored title
        Task task = mTaskTestSubscriber.getOnNextEvents().get(0);
        assertEquals(task.getTitle(), restoredTitle);
        // And all the initial values of the task
        assertEquals(task.getId(), TASK.getId());
        assertEquals(task.getDescription(), TASK.getDescription());
    }

    @Test
    public void restoreTask_withDescriptionUpdated() {
        // Given a task in the repository
        when(mTasksRepository.getTask(TASK.getId())).thenReturn(Observable.just(TASK));
        // Get a reference to the class under test for a task id
        mViewModel = new AddEditTaskViewModel(TASK.getId(), mTasksRepository, mNavigationProvider);

        // When setting a restore description
        String restoredDescription = "new description";
        mViewModel.setRestoredState(null, restoredDescription);
        // When that we are subscribing to the task
        mViewModel.getTask().subscribe(mTaskTestSubscriber);

        // The emitted task has the restored description
        Task task = mTaskTestSubscriber.getOnNextEvents().get(0);
        assertEquals(task.getDescription(), restoredDescription);
        // And all the initial values of the task
        assertEquals(task.getId(), TASK.getId());
        assertEquals(task.getTitle(), TASK.getTitle());
    }
}
