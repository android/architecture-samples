package com.example.android.architecture.blueprints.todoapp.tasks;

import android.app.Activity;
import android.support.annotation.DrawableRes;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;
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
import static com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.ALL_TASKS;
import static com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.COMPLETED_TASKS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link TasksViewModel}
 */
public class TasksViewModelTest {

    private static Task ACTIVE_TASK = new Task("title", "description");
    private static Task COMPLETED_TASK = new Task("title", "description", true);
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

    private TestSubscriber<Integer> mFilterTextSubscriber;

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
        mFilterTextSubscriber = new TestSubscriber<>();
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
    public void getTasks_emits_whenTasks() {
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

    @Test
    public void getNoTasksModel_emits_whenNoTasks_withFilterAll() {
        // Given that we are subscribed to the no tasks stream
        mViewModel.getNoTasks().subscribe(mNoTasksSubscriber);
        // Given that the task repository returns empty task list
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(new ArrayList<>()));

        // When subscribed to the tasks
        mViewModel.getTasks().subscribe(mTasksSubscriber);

        // NoTasks emits
        mNoTasksSubscriber.assertValueCount(1);
        NoTasksModel model = mNoTasksSubscriber.getOnNextEvents().get(0);
        assertEquals(model.getText(), R.string.no_tasks_all);
        assertEquals(model.getIcon(), R.drawable.ic_assignment_turned_in_24dp);
        assertEquals(model.isShowAdd(), true);
    }

    @Test
    public void getNoTasksModel_emits_whenNoTasks_withFilterActive() {
        // Given that we are subscribed to the no tasks stream
        mViewModel.getNoTasks().subscribe(mNoTasksSubscriber);
        // Given that the filtering is active
        mViewModel.filter(ACTIVE_TASKS);
        // Given that the task repository returns empty task list
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(new ArrayList<>()));

        // When subscribed to the tasks
        mViewModel.getTasks().subscribe(mTasksSubscriber);

        // NoTasks emits
        mNoTasksSubscriber.assertValueCount(1);
        NoTasksModel model = mNoTasksSubscriber.getOnNextEvents().get(0);
        assertEquals(model.getText(), R.string.no_tasks_active);
        assertEquals(model.getIcon(), R.drawable.ic_check_circle_24dp);
        assertEquals(model.isShowAdd(), false);
    }

    @Test
    public void getNoTasksModel_emits_whenNoTasks_withFilterCompleted() {
        // Given that we are subscribed to the no tasks stream
        mViewModel.getNoTasks().subscribe(mNoTasksSubscriber);
        // Given that the filtering is completed
        mViewModel.filter(COMPLETED_TASKS);
        // Given that the task repository returns empty task list
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(new ArrayList<>()));

        // When subscribed to the tasks
        mViewModel.getTasks().subscribe(mTasksSubscriber);

        // NoTasks emits
        mNoTasksSubscriber.assertValueCount(1);
        NoTasksModel model = mNoTasksSubscriber.getOnNextEvents().get(0);
        assertEquals(model.getText(), R.string.no_tasks_completed);
        assertEquals(model.getIcon(), R.drawable.ic_verified_user_24dp);
        assertEquals(model.isShowAdd(), false);
    }

    @Test
    public void snackbarEmits_whenTaskAdded_withResultOk() {
        //Given that we are subscribed to the snackbar text
        mViewModel.getSnackbarMessage().subscribe(mSnackbarTextSubscriber);

        // When handling activity result for a task added successfully
        mViewModel.handleActivityResult(AddEditTaskActivity.REQUEST_ADD_TASK, Activity.RESULT_OK);

        // The snackbar text emits correct value
        mSnackbarTextSubscriber.assertValue(R.string.successfully_saved_task_message);
    }

    @Test
    public void snackbarEmits_whenTaskAdded_withResultCanceled() {
        //Given that we are subscribed to the snackbar text
        mViewModel.getSnackbarMessage().subscribe(mSnackbarTextSubscriber);

        // When handling activity result for a task canceled
        mViewModel.handleActivityResult(AddEditTaskActivity.REQUEST_ADD_TASK, Activity.RESULT_CANCELED);

        // The snackbar text does not emit
        mSnackbarTextSubscriber.assertNoValues();
    }

    @Test
    public void filterText_emits_whenFilterAllSet() {
        // Given that we are subscribed to the filter text
        mViewModel.getFilterText().subscribe(mFilterTextSubscriber);

        // When setting the filter to all
        mViewModel.filter(ALL_TASKS);

        // The filter text emits correct value
        mFilterTextSubscriber.assertValues(R.string.label_all);
    }

    @Test
    public void filterText_emits_whenFilterActiveSet() {
        // Given that we are subscribed to the filter text
        mViewModel.getFilterText().subscribe(mFilterTextSubscriber);

        // When setting the filter to active
        mViewModel.filter(ACTIVE_TASKS);

        // The filter text emits correct value
        mFilterTextSubscriber.assertValues(R.string.label_all, R.string.label_active);
    }

    @Test
    public void filterText_emits_whenFilterCompletedSet() {
        // Given that we are subscribed to the filter text
        mViewModel.getFilterText().subscribe(mFilterTextSubscriber);

        // When setting the filter to completed
        mViewModel.filter(COMPLETED_TASKS);

        // The filter text emits correct value
        mFilterTextSubscriber.assertValues(R.string.label_all, R.string.label_completed);
    }

    @Test
    public void taskItem_tapAction_opensActivity() {
        // Given a task
        List<Task> tasks = new ArrayList<>();
        tasks.add(ACTIVE_TASK);
        // Given that the task repository returns tasks
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(tasks));
        // Given that we are subscribed to the tasks
        mViewModel.getTasks().subscribe(mTasksSubscriber);
        // And list of task items is emitted
        List<TaskItem> items = mTasksSubscriber.getOnNextEvents().get(0);
        TaskItem taskItem = items.get(0);

        // When triggering the click action
        taskItem.getOnClickAction().call();

        // The AddEditTaskActivity is opened with the correct request code
        verify(mNavigationProvider).startActivityWithExtra(eq(TaskDetailActivity.class),
                eq(TaskDetailActivity.EXTRA_TASK_ID), eq(ACTIVE_TASK.getId()));
    }

    @Test
    public void taskItem_withActiveTask_tapCheck_completesTask() {
        // Given a active task
        List<Task> tasks = new ArrayList<>();
        tasks.add(ACTIVE_TASK);
        // Given that the task repository returns tasks
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(tasks));
        // Given that we are subscribed to the tasks
        mViewModel.getTasks().subscribe(mTasksSubscriber);
        // And list of task items is emitted
        List<TaskItem> items = mTasksSubscriber.getOnNextEvents().get(0);
        TaskItem taskItem = items.get(0);

        // When triggering the check
        taskItem.getOnCheckAction().call(true);

        // The task is marked as completed
        mTasksRepository.completeTask(ACTIVE_TASK);
    }

    @Test
    public void taskItem_withActiveTask_tapCheck_snackbarMessageIsEmitted() {
        // Given a active task
        List<Task> tasks = new ArrayList<>();
        tasks.add(ACTIVE_TASK);
        // Given that the task repository returns tasks
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(tasks));
        //Given that we are subscribed to the snackbar text
        mViewModel.getSnackbarMessage().subscribe(mSnackbarTextSubscriber);
        // Given that we are subscribed to the tasks
        mViewModel.getTasks().subscribe(mTasksSubscriber);
        // And list of task items is emitted
        List<TaskItem> items = mTasksSubscriber.getOnNextEvents().get(0);
        TaskItem taskItem = items.get(0);

        // When triggering the check
        taskItem.getOnCheckAction().call(true);

        // The snackbar emits a message
        mSnackbarTextSubscriber.assertValue(R.string.task_marked_complete);
    }

    @Test
    public void taskItem_withCompletedTask_tapCheck_activatesTask() {
        // Given a completed task
        List<Task> tasks = new ArrayList<>();
        tasks.add(COMPLETED_TASK);
        // Given that the task repository returns tasks
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(tasks));
        // Given that we are subscribed to the tasks
        mViewModel.getTasks().subscribe(mTasksSubscriber);
        // And list of task items is emitted
        List<TaskItem> items = mTasksSubscriber.getOnNextEvents().get(0);
        TaskItem taskItem = items.get(0);

        // When triggering the check
        taskItem.getOnCheckAction().call(false);

        // The task is marked as active
        mTasksRepository.activateTask(COMPLETED_TASK);
    }

    @Test
    public void taskItem_withCompletedTask_tapCheck_snackbarMessageIsEmitted() {
        // Given a completed task
        List<Task> tasks = new ArrayList<>();
        tasks.add(COMPLETED_TASK);
        // Given that the task repository returns tasks
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(tasks));
        //Given that we are subscribed to the snackbar text
        mViewModel.getSnackbarMessage().subscribe(mSnackbarTextSubscriber);
        // Given that we are subscribed to the tasks
        mViewModel.getTasks().subscribe(mTasksSubscriber);
        // And list of task items is emitted
        List<TaskItem> items = mTasksSubscriber.getOnNextEvents().get(0);
        TaskItem taskItem = items.get(0);

        // When triggering the check
        taskItem.getOnCheckAction().call(false);

        // The snackbar emits a message
        mSnackbarTextSubscriber.assertValue(R.string.task_marked_active);
    }

    @Test
    public void addTask_opensActivity() {
        // When adding a new task
        mViewModel.addNewTask();

        // The AddEditTaskActivity is opened with the correct request code
        verify(mNavigationProvider).startActivityForResult(eq(AddEditTaskActivity.class),
                eq(AddEditTaskActivity.REQUEST_ADD_TASK));
    }

    @Test
    public void clearCompletedTask_clearsCompletedTasksInRepository() {
        // When clearing completed tasks
        mViewModel.clearCompletedTasks();

        // The completed tasks are cleared in the repository
        verify(mTasksRepository).clearCompletedTasks();
    }

    @Test
    public void clearCompletedTask_snackbarMessageIsEmitted() {
        // Given that we are subscribed to the snackbar text
        mViewModel.getSnackbarMessage().subscribe(mSnackbarTextSubscriber);

        // When clearing completed tasks
        mViewModel.clearCompletedTasks();

        // A snackbar text is emitted
        mSnackbarTextSubscriber.assertValue(R.string.completed_tasks_cleared);
    }

    @Test
    public void clearCompletedTask_triggersGetTasksEmission() {

        // When clearing completed tasks
        mViewModel.clearCompletedTasks();

        // The completed tasks are cleared in the repository
        verify(mTasksRepository).clearCompletedTasks();
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
