package com.example.android.architecture.blueprints.todoapp.addedittask;

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
        // Get a reference to the class under test for the same task id
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
        // Get a reference to the class under test for the same task id
        mViewModel = new AddEditTaskViewModel(TASK.getId(), mTasksRepository, mNavigationProvider);
        // With subscribed to the snackbar
        mViewModel.getSnackbarText().subscribe(mSnackbarTestSubscriber);

        // When subscribing to the task
        mViewModel.getTask().subscribe(mTaskTestSubscriber);
        
        // The correct resource id is emitted
        mSnackbarTestSubscriber.assertValue(R.string.empty_task_message);
    }
}
