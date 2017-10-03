package com.example.android.architecture.blueprints.todoapp.tasks;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.Pair;
import android.util.Log;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.BaseSchedulerProvider;

import java.util.List;

import rx.Completable;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ViewModel for the list of tasks.
 */
public final class TasksViewModel {

    @VisibleForTesting
    static final String FILTER_KEY = "filter";
    private static final String TAG = TasksViewModel.class.getSimpleName();

    @NonNull
    private final TasksRepository mTasksRepository;

    @NonNull
    private final TasksNavigator mNavigator;

    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;

    // using a BehaviourSubject because we are interested in the last object that was emitted before
    // subscribing. Like this we ensure that the loading indicator has the correct visibility.
    private final BehaviorSubject<Boolean> mLoadingIndicatorSubject;

    // using a BehaviourSubject because we are interested in the last object that was emitted before
    // subscribing. Like this we ensure that the last selected filter or the default one is used.
    @NonNull
    private final BehaviorSubject<TasksFilterType> mFilter;

    // using a PublishSubject because we are not interested in the last object that was emitted
    // before subscribing. Like this we avoid displaying the snackbar multiple times
    @NonNull
    private final PublishSubject<Integer> mSnackbarText;

    public TasksViewModel(@NonNull TasksRepository tasksRepository,
                          @NonNull TasksNavigator navigationProvider,
                          @NonNull BaseSchedulerProvider schedulerProvider) {
        mTasksRepository = checkNotNull(tasksRepository, "TaskRepository cannot be null");
        mNavigator = checkNotNull(navigationProvider, "Navigator cannot be null");
        mSchedulerProvider = checkNotNull(schedulerProvider, "SchedulerProvider cannot be null");

        mLoadingIndicatorSubject = BehaviorSubject.create(false);
        mFilter = BehaviorSubject.create(TasksFilterType.ALL_TASKS);
        mSnackbarText = PublishSubject.create();
    }


    /**
     * @return the model for the tasks list.
     */
    @NonNull
    public Observable<TasksUiModel> getUiModel() {
        return getTaskItems()
                .doOnSubscribe(() -> mLoadingIndicatorSubject.onNext(true))
                .doOnNext(__ -> mLoadingIndicatorSubject.onNext(false))
                .doOnError(__ -> mSnackbarText.onNext(R.string.loading_tasks_error))
                .switchMap(tasks -> mFilter.map(filterType -> Pair.create(tasks, filterType)))
                .map(this::constructTasksModel);
    }

    @NonNull
    private TasksUiModel constructTasksModel(Pair<List<TaskItem>, TasksFilterType> pair) {
        List<TaskItem> tasks = pair.first;
        TasksFilterType filterType = pair.second;

        int filterTextResId = getFilterText(filterType);
        boolean isTasksListVisible = !tasks.isEmpty();
        boolean isNoTasksViewVisible = !isTasksListVisible;
        NoTasksModel noTasksModel = null;
        if (tasks.isEmpty()) {
            noTasksModel = getNoTasksModel(filterType);
        }

        return new TasksUiModel(filterTextResId, isTasksListVisible, tasks, isNoTasksViewVisible,
                noTasksModel);
    }

    private Observable<List<TaskItem>> getTaskItems() {
        return Observable.combineLatest(mTasksRepository.getTasks(),
                mFilter,
                Pair::create)
                .flatMap(pair -> Observable.from(pair.first)
                        .filter(task -> shouldFilterTask(task, pair.second))
                        .map(this::constructTaskItem)
                        .toList());
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
        mNavigator.openTaskDetails(task.getId());
    }

    private void handleTaskChecked(Task task, boolean checked) {
        Completable checkTask = checked ? completeTask(task) : activateTask(task);
        checkTask.subscribeOn(mSchedulerProvider.computation())
                .observeOn(mSchedulerProvider.computation())
                .subscribe(
                        //on Completed
                        () -> {
                        },
                        // on error
                        throwable -> Log.e(TAG, "Error completing or activating task")
                );
    }

    private Completable completeTask(Task completedTask) {
        return mTasksRepository.completeTask(completedTask)
                .doOnCompleted(() -> mSnackbarText.onNext(R.string.task_marked_complete));
    }

    private Completable activateTask(Task activeTask) {
        return mTasksRepository.activateTask(activeTask)
                .doOnCompleted(() -> mSnackbarText.onNext(R.string.task_marked_active));
    }

    /**
     * Trigger a force update of the tasks.
     */
    public Completable forceUpdateTasks() {
        mLoadingIndicatorSubject.onNext(true);
        return mTasksRepository.refreshTasks()
                .doOnTerminate(() -> mLoadingIndicatorSubject.onNext(false));
    }

    /**
     * Open the {@link AddEditTaskActivity}
     */
    public void addNewTask() {
        mNavigator.addNewTask();
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
     * Restore the state of the view based on a bundle.
     *
     * @param bundle the bundle containing the state.
     */
    public void restoreState(@Nullable Bundle bundle) {
        if (bundle != null && bundle.containsKey(FILTER_KEY)) {
            TasksFilterType filterType = (TasksFilterType) bundle.getSerializable(FILTER_KEY);
            mFilter.onNext(filterType);
        }
    }

    /**
     * @return the state of the view that needs to be saved.
     */
    @NonNull
    public Bundle getStateToSave() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(FILTER_KEY, mFilter.getValue());
        return bundle;
    }

    /**
     * Clear the list of completed tasks and refresh the list.
     *
     * @return a Completable that emits when the tasks are cleared or error.
     */
    @NonNull
    public Completable clearCompletedTasks() {
        return Completable.fromAction(this::clearCompletedTasksAndNotify);
    }

    private void clearCompletedTasksAndNotify() {
        mTasksRepository.clearCompletedTasks();
        mSnackbarText.onNext(R.string.completed_tasks_cleared);
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
    public Observable<Boolean> getLoadingIndicatorVisibility() {
        return mLoadingIndicatorSubject.asObservable();
    }
}