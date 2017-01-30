package com.example.android.architecture.blueprints.todoapp.tasks;

import android.support.annotation.DrawableRes;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.providers.BaseNavigationProvider;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

import static com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.ACTIVE_TASKS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link TasksViewModel}
 */
public class TasksViewModelTest {
    private static List<Task> TASKS;

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private BaseNavigationProvider mNavigationProvider;

    private TasksViewModel mViewModel;

    private TestSubscriber<List<TaskItem>> mTasksSubscriber;

    private TestSubscriber<NoTasksModel> mNoTasksSubscriber;

    private TestSubscriber<Boolean> mProgressIndicatorSubscriber;

    private TestSubscriber<Integer> mSnackbarTextSubscriber;

    @Before
    public void setupTasksPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mViewModel = new TasksViewModel(mTasksRepository, mNavigationProvider);

        // We subscribe the tasks to 3, with one active and two completed
        TASKS = Lists.newArrayList(new Task("Title1", "Description1"),
                new Task("Title2", "Description2", true), new Task("Title3", "Description3", true));

        mTasksSubscriber = new TestSubscriber<>();
        mNoTasksSubscriber = new TestSubscriber<>();
        mProgressIndicatorSubscriber = new TestSubscriber<>();
        mSnackbarTextSubscriber = new TestSubscriber<>();
    }

    @Test
    public void getTasks_doesNotEmit_whenNoTasks() {
        // Given that the task repository returns empty task list
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(new ArrayList<>()));

        // When subscribed to the tasks
        mViewModel.getTasks().subscribe(mTasksSubscriber);

        // Nothing is emitted
        mTasksSubscriber.assertNoValues();
        mTasksSubscriber.assertNoErrors();
    }

    @Test
    public void progressIndicator_emits_whenSubscribedToTasks() {
        // Given that we are subscribed to the progress indicator
        mViewModel.getProgressIndicator().subscribe(mProgressIndicatorSubscriber);

        // When subscribed to the tasks
        mViewModel.getTasks().subscribe();

        // The progress indicator emits true after an initial value of false
        mProgressIndicatorSubscriber.assertValues(false, true);
    }

    @Test
    public void snackbarText_emits_whenError_whenRetrievingTasks() {
        // Given an error when retrieving tasks
        when(mTasksRepository.getTasks()).thenReturn(Observable.error(new RuntimeException()));
        // Given that we are subscribed to the snackbar text
        mViewModel.getSnackbarMessage().subscribe(mSnackbarTextSubscriber);

        // When subscribed to the tasks
        mViewModel.getTasks().subscribe(mTasksSubscriber);

        mSnackbarTextSubscriber.assertValue(R.string.loading_tasks_error);
    }

    @Test
    public void geTasks_emits_whenTasks() {
        // Given that the task repository returns tasks
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(TASKS));

        // When subscribed to the tasks
        mViewModel.getTasks().subscribe(mTasksSubscriber);

        // A list of task items is emitted
        mTasksSubscriber.assertValueCount(1);
        List<TaskItem> items = mTasksSubscriber.getOnNextEvents().get(0);
        assertTaskItems(items);
    }

    @Test
    public void getTask_emits_whenFilterSet() {
        // Given that the task repository returns tasks
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(TASKS));
        // Given that we are subscribed to the tasks
        mViewModel.getTasks().subscribe(mTasksSubscriber);

        // When setting a new filter
        mViewModel.filter(ACTIVE_TASKS);

        // Two lists of task items are emitted: one for the initial value of the filter
        // and another one for the new filter
        mTasksSubscriber.assertValueCount(2);
        // And the 2nd list contains only one value
        List<TaskItem> items = mTasksSubscriber.getOnNextEvents().get(1);
        assertEquals(items.size(), 1);
        // And the TaskItem is the active task
        assertTask(items.get(0), TASKS.get(0), R.drawable.touch_feedback);
    }

    @Test
    public void getTask_emits_whenForceUpdateTasks() {
        // Given that the task repository returns tasks
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(TASKS));
        // Given that we are subscribed to the tasks
        mViewModel.getTasks().subscribe(mTasksSubscriber);

        // When calling force update
        mViewModel.forceUpdateTasks();

        // Two lists of the task items are emitted, both with the same value
        mTasksSubscriber.assertValueCount(2);
        List<TaskItem> items = mTasksSubscriber.getOnNextEvents().get(1);
        assertTaskItems(items);
    }

    @Test
    public void getTask_emits_whenUpdateTasks() {
        // Given that the task repository returns tasks
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(TASKS));
        // Given that we are subscribed to the tasks
        mViewModel.getTasks().subscribe(mTasksSubscriber);

        // When calling update tasks
        mViewModel.updateTasks();

        // Two lists of the task items are emitted, both with the same value
        mTasksSubscriber.assertValueCount(2);
        List<TaskItem> items = mTasksSubscriber.getOnNextEvents().get(1);
        assertTaskItems(items);
    }

    @Test
    public void progressIndicator_emits_whenForceUpdatingTasks() {
        // Given that we are subscribed to the progress indicator
        mViewModel.getProgressIndicator().subscribe(mProgressIndicatorSubscriber);
        // Given that we are subscribed to the tasks
        mViewModel.getTasks().subscribe();

        // When calling force update
        mViewModel.forceUpdateTasks();

        // The progress indicator emits true
        mProgressIndicatorSubscriber.assertValues(false, true, true);
    }

    @Test
    public void forceUpdateTasks_updatesTasksRepository() {
        // Given that we are subscribed to the tasks
        mViewModel.getTasks().subscribe();

        // When calling force update
        mViewModel.forceUpdateTasks();

        // The tasks are refreshed first the first time when subscribed and then when
        // calling forceUpdateTasks
        verify(mTasksRepository, times(2)).refreshTasks();
    }

    private void assertTaskItems(List<TaskItem> items) {
        // check if the TaskItems are the expected ones
        assertEquals(items.size(), TASKS.size());

        assertTask(items.get(0), TASKS.get(0), R.drawable.touch_feedback);
        assertTask(items.get(1), TASKS.get(1), R.drawable.list_completed_touch_feedback);
        assertTask(items.get(2), TASKS.get(2), R.drawable.list_completed_touch_feedback);
    }

    private void assertTask(TaskItem taskItem, Task task, @DrawableRes int resId) {
        assertEquals(taskItem.getTask(), task);
        assertEquals(taskItem.getBackground(), resId);
        assertNotNull(taskItem.getOnCheckAction());
        assertNotNull(taskItem.getOnClickAction());

    }
}
