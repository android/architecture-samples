package com.example.android.architecture.blueprints.todoapp.tasks;

import android.app.Activity;
import android.support.annotation.DrawableRes;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.ImmediateSchedulerProvider;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import rx.Completable;
import rx.Observable;
import rx.observers.TestSubscriber;

import static com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.ACTIVE_TASKS;
import static com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.ALL_TASKS;
import static com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.COMPLETED_TASKS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.eq;
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
    private TasksNavigator mNavigatior;

    private TasksViewModel mViewModel;

    private TestSubscriber<TasksUiModel> mTasksSubscriber;

    private TestSubscriber<Boolean> mProgressIndicatorSubscriber;

    private TestSubscriber<Integer> mSnackbarTextSubscriber;

    private TestSubscriber mCompletableSubscriber;

    @Before
    public void setupTasksPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mViewModel = new TasksViewModel(mTasksRepository, mNavigatior,
                new ImmediateSchedulerProvider());

        // We subscribe the tasks to 3, with one active and two completed
        TASKS = Lists.newArrayList(new Task("Title1", "Description1"),
                new Task("Title2", "Description2", true), new Task("Title3", "Description3", true));

        mTasksSubscriber = new TestSubscriber<>();
        mProgressIndicatorSubscriber = new TestSubscriber<>();
        mSnackbarTextSubscriber = new TestSubscriber<>();
        mCompletableSubscriber = new TestSubscriber<>();
    }

    @Test
    public void progressIndicator_emits_whenSubscribedToTasks() {
        // Given that we are subscribed to the progress indicator
        mViewModel.getProgressIndicator().subscribe(mProgressIndicatorSubscriber);

        // When subscribed to the tasks model
        mViewModel.getTasksModel().subscribe();

        // The progress indicator emits true after an initial value of false
        mProgressIndicatorSubscriber.assertValues(false, true);
    }

    @Test
    public void snackbarText_emits_whenError_whenRetrievingTasks() {
        // Given an error when retrieving tasks
        when(mTasksRepository.getTasks()).thenReturn(Observable.error(new RuntimeException()));
        // Given that we are subscribed to the snackbar text
        mViewModel.getSnackbarMessage().subscribe(mSnackbarTextSubscriber);

        // When subscribed to the tasks model
        mViewModel.getTasksModel().subscribe(mTasksSubscriber);

        // The snackbar emits an error message
        mSnackbarTextSubscriber.assertValue(R.string.loading_tasks_error);
    }

    @Test
    public void getTasksModel_emits_whenTasks() {
        // Given that we are subscribed to the emissions of the UI model
        withTasksInRepositoryAndSubscribed(TASKS);

        // The tasks model containing the list of tasks is emitted
        mTasksSubscriber.assertValueCount(1);
        TasksUiModel model = mTasksSubscriber.getOnNextEvents().get(0);
        assertTasksModelWithTasksVisible(model);
    }

    @Test
    public void getTask_emits_whenFilterSet() {
        // Given that we are subscribed to the emissions of the UI model
        withTasksInRepositoryAndSubscribed(TASKS);

        // When setting a new filter
        mViewModel.filter(ACTIVE_TASKS);

        // Two tasks models are emitted: one for the initial value of the filter
        // and another one for the new filter
        mTasksSubscriber.assertValueCount(2);
        // And the 2nd tasks models list contains only one value
        TasksUiModel model = mTasksSubscriber.getOnNextEvents().get(1);
        assertEquals(model.getItemList().size(), 1);
        // And the TaskItem is the active task
        assertTask(model.getItemList().get(0), TASKS.get(0), R.drawable.touch_feedback);
    }

    @Test
    public void forceUpdateTasks_completes_wheTasksRefereshed() {
        // Given that the task repository refresh completes
        when(mTasksRepository.refreshTasks()).thenReturn(Completable.complete());

        // When calling force update
        mViewModel.forceUpdateTasks().subscribe(mCompletableSubscriber);

        // The force update completable completes
        mCompletableSubscriber.assertCompleted();
    }

    @Test
    public void getProgressIndicator_emits_whenForceUpdateTasks() {
        // Given that the task repository refresh completes
        when(mTasksRepository.refreshTasks()).thenReturn(Completable.complete());
        // Given that we are subscribed to the progress indicator
        mViewModel.getProgressIndicator().subscribe(mProgressIndicatorSubscriber);

        // When calling force update
        mViewModel.forceUpdateTasks().subscribe(mCompletableSubscriber);

        // The progress indicator emits 3 times:
        // with initial value false
        // once true when the force update is called
        // once false, when the force update completes
        mProgressIndicatorSubscriber.assertValues(false, true, false);
    }

    @Test
    public void forceUpdateTasks_updatesTasksRepository() {
        // Given that refresh tasks completes
        when(mTasksRepository.refreshTasks()).thenReturn(Completable.complete());

        // When calling force update
        mViewModel.forceUpdateTasks();

        // The tasks are refreshed in the repository
        verify(mTasksRepository).refreshTasks();
    }

    @Test
    public void geTasksModel_emits_whenNoTasks_withFilterAll() {
        // Given that we are subscribed to the tasks model
        mViewModel.getTasksModel().subscribe(mTasksSubscriber);
        // Given that the task repository returns empty task list
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(new ArrayList<>()));

        // When subscribed to the tasks
        mViewModel.getTasksModel().subscribe(mTasksSubscriber);

        // NoTasks emits
        mTasksSubscriber.assertValueCount(1);
        TasksUiModel model = mTasksSubscriber.getOnNextEvents().get(0);
        assertTasksModelWithNoTasksVisible(model);
        assertNoTasks(model.getNoTasksModel(), R.string.no_tasks_all,
                R.drawable.ic_assignment_turned_in_24dp, true);
    }

    @Test
    public void getTasksModel_emits_whenNoTasks_withFilterActive() {
        // Given that we are subscribed to the tasks model
        mViewModel.getTasksModel().subscribe(mTasksSubscriber);
        // Given that the filtering is active
        mViewModel.filter(ACTIVE_TASKS);
        // Given that the task repository returns empty task list
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(new ArrayList<>()));

        // When subscribed to the tasks
        mViewModel.getTasksModel().subscribe(mTasksSubscriber);

        // TasksUiModel emits with no tasks
        mTasksSubscriber.assertValueCount(1);
        TasksUiModel model = mTasksSubscriber.getOnNextEvents().get(0);
        assertTasksModelWithNoTasksVisible(model);
        assertNoTasks(model.getNoTasksModel(), R.string.no_tasks_active,
                R.drawable.ic_check_circle_24dp, false);
    }

    @Test
    public void getTasksModel_emits_whenNoTasks_withFilterCompleted() {
        // Given that we are subscribed to the tasks model
        mViewModel.getTasksModel().subscribe(mTasksSubscriber);
        // Given that the filtering is completed
        mViewModel.filter(COMPLETED_TASKS);
        // Given that the task repository returns empty task list
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(new ArrayList<>()));

        // When subscribed to the tasks
        mViewModel.getTasksModel().subscribe(mTasksSubscriber);

        // TasksUiModel emits with no tasks
        mTasksSubscriber.assertValueCount(1);
        TasksUiModel model = mTasksSubscriber.getOnNextEvents().get(0);
        assertTasksModelWithNoTasksVisible(model);
        assertNoTasks(model.getNoTasksModel(), R.string.no_tasks_completed,
                R.drawable.ic_verified_user_24dp, false);
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
    public void getTaskModelEmits_whenTaskAdded_withResultOk() {
        // Given that we are subscribed to the emissions of the UI model
        withTasksInRepositoryAndSubscribed(TASKS);

        // When handling activity result for a task added successfully
        mViewModel.handleActivityResult(AddEditTaskActivity.REQUEST_ADD_TASK, Activity.RESULT_OK);

        // The mTasksSubscriber emits an initial value and then emits again
        mTasksSubscriber.assertValueCount(2);
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
    public void tasksModel_emits_whenFilterAllSet() {
        // Given that we are subscribed to the emissions of the UI model
        withTaskInRepositoryAndSubscribed(ACTIVE_TASK);

        // When setting the filter to all
        mViewModel.filter(ALL_TASKS);

        // The UI model contains the correct filter text
        TasksUiModel model = mTasksSubscriber.getOnNextEvents().get(1);
        assertEquals(model.getFilterResId(), R.string.label_all);
    }

    @Test
    public void tasksModel_emits_whenFilterActiveSet() {
        // Given that we are subscribed to the emissions of the UI model
        withTaskInRepositoryAndSubscribed(ACTIVE_TASK);

        // When setting the filter to active
        mViewModel.filter(ACTIVE_TASKS);

        // The UI model contains the correct filter text
        TasksUiModel model = mTasksSubscriber.getOnNextEvents().get(1);
        assertEquals(model.getFilterResId(), R.string.label_active);
    }

    @Test
    public void tasksModel_emits_whenFilterCompletedSet() {
        // Given that we are subscribed to the emissions of the UI model
        withTaskInRepositoryAndSubscribed(ACTIVE_TASK);

        // When setting the filter to completed
        mViewModel.filter(COMPLETED_TASKS);

        // The UI model contains the correct filter text
        TasksUiModel model = mTasksSubscriber.getOnNextEvents().get(1);
        assertEquals(model.getFilterResId(), R.string.label_completed);
    }

    @Test
    public void taskItem_tapAction_opensTaskDetails() {
        // Given a task
        withTaskInRepositoryAndSubscribed(ACTIVE_TASK);
        // And list of task items is emitted
        List<TaskItem> items = mTasksSubscriber.getOnNextEvents().get(0).getItemList();
        TaskItem taskItem = items.get(0);

        // When triggering the click action
        taskItem.getOnClickAction().call();

        // Opening of the task details is called with the correct task id
        verify(mNavigatior).openTaskDetails(eq(ACTIVE_TASK.getId()));
    }

    @Test
    public void taskItem_withActiveTask_tapCheck_completesTask() {
        // Given a active task
        withTaskInRepositoryAndSubscribed(ACTIVE_TASK);
        withTaskCompleted(ACTIVE_TASK);
        // And list of task items is emitted
        List<TaskItem> items = mTasksSubscriber.getOnNextEvents().get(0).getItemList();
        TaskItem taskItem = items.get(0);

        // When triggering the check
        taskItem.getOnCheckAction().call(true);

        // The task is marked as completed
        verify(mTasksRepository).completeTask(ACTIVE_TASK);
    }

    @Test
    public void taskItem_withActiveTask_tapCheck_snackbarMessageIsEmitted() {
        // Given a active task
        withTaskInRepositoryAndSubscribed(ACTIVE_TASK);
        withTaskCompleted(ACTIVE_TASK);
        //Given that we are subscribed to the snackbar text
        mViewModel.getSnackbarMessage().subscribe(mSnackbarTextSubscriber);
        // And list of task items is emitted
        List<TaskItem> items = mTasksSubscriber.getOnNextEvents().get(0).getItemList();
        TaskItem taskItem = items.get(0);

        // When triggering the check
        taskItem.getOnCheckAction().call(true);

        // The snackbar emits a message
        mSnackbarTextSubscriber.assertValue(R.string.task_marked_complete);
    }

    @Test
    public void taskItem_withCompletedTask_tapCheck_activatesTask() {
        // Given a completed task
        withTaskInRepositoryAndSubscribed(COMPLETED_TASK);
        withTaskActivated(COMPLETED_TASK);
        // And list of task items is emitted
        List<TaskItem> items = mTasksSubscriber.getOnNextEvents().get(0).getItemList();
        TaskItem taskItem = items.get(0);

        // When triggering the check
        taskItem.getOnCheckAction().call(false);

        // The task is marked as active
        mTasksRepository.activateTask(COMPLETED_TASK);
    }

    @Test
    public void taskItem_withCompletedTask_tapCheck_snackbarMessageIsEmitted() {
        // Given a completed task
        withTaskInRepositoryAndSubscribed(COMPLETED_TASK);
        // Given that the task is activated successfully
        withTaskActivated(COMPLETED_TASK);
        //Given that we are subscribed to the snackbar text
        mViewModel.getSnackbarMessage().subscribe(mSnackbarTextSubscriber);
        // And list of task items is emitted
        List<TaskItem> items = mTasksSubscriber.getOnNextEvents().get(0).getItemList();
        TaskItem taskItem = items.get(0);

        // When triggering the check
        taskItem.getOnCheckAction().call(false);

        // The snackbar emits a message
        mSnackbarTextSubscriber.assertValue(R.string.task_marked_active);
    }

    @Test
    public void addTask_callsAddNewTask() {
        // When adding a new task
        mViewModel.addNewTask();

        // The add new task method is called
        verify(mNavigatior).addNewTask();
    }

    @Test
    public void clearCompletedTask_clearsCompletedTasksInRepository() {
        // When subscribing to the clearing of completed tasks
        mViewModel.clearCompletedTasks().subscribe(mCompletableSubscriber);

        // The Observable completes
        mCompletableSubscriber.assertCompleted();
        // The completed tasks are cleared in the repository
        verify(mTasksRepository).clearCompletedTasks();
    }

    @Test
    public void clearCompletedTask_snackbarMessageIsEmitted() {
        // Given that we are subscribed to the snackbar text
        mViewModel.getSnackbarMessage().subscribe(mSnackbarTextSubscriber);

        // When clearing completed tasks
        mViewModel.clearCompletedTasks().subscribe();

        // A snackbar text is emitted
        mSnackbarTextSubscriber.assertValue(R.string.completed_tasks_cleared);
    }

    @Test
    public void clearCompletedTask_triggersGetTasksEmission() {
        // Given that we are subscribed to the emissions of the UI model
        withTasksInRepositoryAndSubscribed(TASKS);

        // When clearing completed tasks
        mViewModel.clearCompletedTasks().subscribe();

        // The mTasksSubscriber emits an initial value and then emits again
        mTasksSubscriber.assertValueCount(2);
    }

    private void assertTasksModelWithNoTasksVisible(TasksUiModel model) {
        assertFalse(model.isTasksListVisible());
        assertTrue(model.getItemList().isEmpty());
        assertTrue(model.isNoTasksViewVisible());
    }

    private void assertNoTasks(NoTasksModel model, int expectedText, int expectedIcon,
                               boolean expectedShowAdd) {
        assertEquals(model.getText(), expectedText);
        assertEquals(model.getIcon(), expectedIcon);
        assertEquals(model.isShowAdd(), expectedShowAdd);
    }

    private void assertTasksModelWithTasksVisible(TasksUiModel model) {
        assertTrue(model.isTasksListVisible());
        assertTaskItems(model.getItemList());
        assertFalse(model.isNoTasksViewVisible());
        assertNull(model.getNoTasksModel());
        assertEquals(model.getFilterResId(), R.string.label_all);
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

    private void withTaskInRepositoryAndSubscribed(Task task) {
        List<Task> tasks = new ArrayList<>();
        tasks.add(task);
        withTasksInRepositoryAndSubscribed(tasks);
    }

    private void withTasksInRepositoryAndSubscribed(List<Task> tasks){
        // Given that the task repository returns tasks
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(tasks));
        // Given that we are subscribed to the tasks
        mViewModel.getTasksModel().subscribe(mTasksSubscriber);
    }

    private void withTaskCompleted(Task task) {
        when(mTasksRepository.completeTask(task)).thenReturn(Completable.complete());
    }

    private void withTaskActivated(Task task) {
        when(mTasksRepository.activateTask(task)).thenReturn(Completable.complete());
    }
}
