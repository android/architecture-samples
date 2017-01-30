package com.example.android.architecture.blueprints.todoapp.tasks;

import android.app.Activity;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.util.Pair;
import android.util.Log;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.providers.BaseNavigationProvider;

import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ViewModel for the list of tasks.
 */
public final class TasksViewModel {

    private static final String TAG = TasksViewModel.class.getSimpleName();

    @NonNull
    private final TasksRepository mTasksRepository;

    @NonNull
    private final BaseNavigationProvider mNavigationProvider;

    @NonNull
    private final BehaviorSubject<Boolean> mProgressIndicatorSubject;

    @NonNull
    private final BehaviorSubject<TasksFilterType> mFilter;

    @NonNull
    private final BehaviorSubject<Boolean> mTriggerForceUpdate;

    @NonNull
    private final PublishSubject<Integer> mSnackbarText;

    @NonNull
    private final PublishSubject<NoTasksModel> mNoTasks;

    public TasksViewModel(@NonNull TasksRepository tasksRepository,
                          @NonNull BaseNavigationProvider navigationProvider) {
        mTasksRepository = checkNotNull(tasksRepository, "TaskRepository cannot be null");
        mNavigationProvider = checkNotNull(navigationProvider, "NavigationProvider cannot be null");
        mProgressIndicatorSubject = BehaviorSubject.create(false);
        mFilter = BehaviorSubject.create(TasksFilterType.ALL_TASKS);
        mTriggerForceUpdate = BehaviorSubject.create(true);
        mSnackbarText = PublishSubject.create();
        mNoTasks = PublishSubject.create();
    }

    /**
     * @return the list of tasks.
     */
    @NonNull
    public Observable<List<TaskItem>> getTasks() {
        return getTaskItems()
                .doOnSubscribe(() -> mProgressIndicatorSubject.onNext(true))
                .doOnNext(__ -> mProgressIndicatorSubject.onNext(false))
                .doOnError(__ -> mSnackbarText.onNext(R.string.loading_tasks_error))
                .doOnNext(this::handleTasks)
                .filter(tasks -> !tasks.isEmpty());
    }

    private Observable<List<TaskItem>> getTaskItems() {
        return Observable.combineLatest(getTasksFromRepo(),
                mFilter,
                Pair::create)
                .flatMap(pair -> Observable.from(pair.first)
                        .filter(task -> shouldFilterTask(task, pair.second))
                        .map(this::constructTaskItem)
                        .toList());
    }

    private void handleTasks(List<TaskItem> tasks) {
        if (tasks.isEmpty()) {
            mFilter.map(this::getNoTasksModel)
                    .subscribe(
                            //onNext
                            mNoTasks::onNext,
                            //onError
                            error -> Log.e(TAG, "Error handling no tasks ", error));
        }
    }

    private NoTasksModel getNoTasksModel(TasksFilterType mCurrentFiltering) {
        switch (mCurrentFiltering) {
            case ACTIVE_TASKS:
                return new NoTasksModel(R.string.no_tasks_active,
                        R.drawable.ic_check_circle_24dp, false);
            case COMPLETED_TASKS:
                return new NoTasksModel(R.string.no_tasks_completed,
                        R.drawable.ic_verified_user_24dp, false);
            default:
                return new NoTasksModel(R.string.no_tasks_all,
                        R.drawable.ic_assignment_turned_in_24dp, true);
        }
    }

    /**
     * @return an Observable that emits when there are no tasks, with the reason why there are no tasks.
     */
    @NonNull
    public Observable<NoTasksModel> getNoTasks() {
        return mNoTasks.asObservable();
    }

    @NonNull
    private Observable<List<Task>> getTasksFromRepo() {
        return mTriggerForceUpdate
                .doOnNext(this::forceRefreshTasks)
                .flatMap(__ -> mTasksRepository.getTasks());
    }

    @NonNull
    private TaskItem constructTaskItem(Task task) {
        @DrawableRes int background = task.isCompleted()
                ? R.drawable.list_completed_touch_feedback
                : R.drawable.touch_feedback;

        return new TaskItem(task, background,
                () -> handleTaskTaped(task),
                checked -> handleTaskChecked(task, checked));
    }

    private void handleTaskTaped(Task task) {
        mNavigationProvider.startActivityWithExtra(AddEditTaskActivity.class,
                ARGUMENT_EDIT_TASK_ID, task.getId());
    }

    private void handleTaskChecked(Task task, boolean checked) {
        if (checked) {
            completeTask(task);
        } else {
            activateTask(task);
        }
        mTriggerForceUpdate.onNext(false);
    }

    private void completeTask(Task completedTask) {
        mTasksRepository.completeTask(completedTask);
        mSnackbarText.onNext(R.string.task_marked_complete);
    }

    private void activateTask(Task activeTask) {
        mTasksRepository.activateTask(activeTask);
        mSnackbarText.onNext(R.string.task_marked_active);
    }


    private void forceRefreshTasks(boolean force) {
        if (force) {
            mTasksRepository.refreshTasks();
        }
    }

    /**
     * Trigger a force update of the tasks.
     */
    public void forceUpdateTasks() {
        mProgressIndicatorSubject.onNext(true);
        mTriggerForceUpdate.onNext(true);
    }

    /**
     * Update the list of tasks.
     */
    public void updateTasks() {
        mTriggerForceUpdate.onNext(false);
    }

    /**
     * Open the {@link AddEditTaskActivity}
     */
    public void addNewTask() {
        mNavigationProvider.startActivityForResult(AddEditTaskActivity.class,
                AddEditTaskActivity.REQUEST_ADD_TASK);
    }

    /**
     * Handle the response received on onActivityResult.
     *
     * @param requestCode the request with which the Activity was opened.
     * @param resultCode  the result of the Activity.
     */
    public void handleActivityResult(int requestCode, int resultCode) {
        // If a task was successfully added, show snackbar
        if (AddEditTaskActivity.REQUEST_ADD_TASK == requestCode
                && Activity.RESULT_OK == resultCode) {
            mSnackbarText.onNext(R.string.successfully_saved_task_message);
        }
    }

    @NonNull
    private Boolean shouldFilterTask(Task task, TasksFilterType filter) {
        switch (filter) {
            case ACTIVE_TASKS:
                return task.isActive();
            case COMPLETED_TASKS:
                return task.isCompleted();
            case ALL_TASKS:
            default:
                return true;
        }
    }

    @StringRes
    private int getFilterText(TasksFilterType filter) {
        switch (filter) {
            case ACTIVE_TASKS:
                return R.string.label_active;
            case COMPLETED_TASKS:
                return R.string.label_completed;
            case ALL_TASKS:
            default:
                return R.string.label_all;
        }
    }

    /**
     * Sets the current task filtering type.
     *
     * @param filter Can be {@link TasksFilterType#ALL_TASKS},
     *               {@link TasksFilterType#COMPLETED_TASKS}, or
     *               {@link TasksFilterType#ACTIVE_TASKS}
     */
    public void filter(TasksFilterType filter) {
        mFilter.onNext(filter);
    }

    /**
     * @return the filter text.
     */
    @NonNull
    public Observable<Integer> getFilterText() {
        return mFilter.map(this::getFilterText)
                .distinctUntilChanged();
    }

    /**
     * Clear the list of completed tasks and refresh the list.
     */
    public void clearCompletedTasks() {
        mTasksRepository.clearCompletedTasks();
        mSnackbarText.onNext(R.string.completed_tasks_cleared);
        mTriggerForceUpdate.onNext(false);
    }

    /**
     * @return a stream of string ids that should be displayed in the snackbar.
     */
    @NonNull
    public Observable<Integer> getSnackbarMessage() {
        return mSnackbarText.asObservable();
    }

    /**
     * @return a stream that emits true if the progress indicator should be displayed, false otherwise.
     */
    @NonNull
    public Observable<Boolean> getProgressIndicator() {
        return mProgressIndicatorSubject.asObservable();
    }
}