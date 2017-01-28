package com.example.android.architecture.blueprints.todoapp.tasks;

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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ViewModel for the list of tasks.
 */
public class TasksViewModel {

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
    public Observable<List<Task>> getTasks() {
        return Observable.combineLatest(getTasksFromRepo(),
                mFilter,
                Pair::create)
                .flatMap(pair -> Observable.from(pair.first)
                        .filter(task -> shouldFilterTask(task, pair.second))
                        .toList())
                .doOnSubscribe(() -> mProgressIndicatorSubject.onNext(true))
                .doOnNext(__ -> mProgressIndicatorSubject.onNext(false))
                .doOnNext(this::handleTasks)
                .doOnError(__ -> mSnackbarText.onNext(R.string.loading_tasks_error))
                .filter(List::isEmpty);
    }

    private void handleTasks(List<Task> tasks) {
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
                return new NoTasksModel(R.string.no_tasks_active, R.drawable.ic_check_circle_24dp, false);
            case COMPLETED_TASKS:
                return new NoTasksModel(R.string.no_tasks_completed, R.drawable.ic_verified_user_24dp, false);
            default:
                return new NoTasksModel(R.string.no_tasks_all, R.drawable.ic_assignment_turned_in_24dp, true);
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

    private void forceRefreshTasks(boolean force) {
        if (force) {
            mTasksRepository.refreshTasks();
        }
    }

    /**
     * Trigger a force update of the tasks.
     */
    public void forceUpdateTasks() {
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
        mNavigationProvider.startActivityForResult(AddEditTaskActivity.class, AddEditTaskActivity.REQUEST_ADD_TASK);
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
    public Observable<Integer> getFilter() {
        return mFilter.map(this::getFilterText);
    }

    public void clearCompletedTasks() {
        mTasksRepository.clearCompletedTasks();
        mSnackbarText.onNext(R.string.completed_tasks_cleared);
        mTriggerForceUpdate.onNext(false);
    }

    @NonNull
    public Observable<Integer> getSnackbarMessage() {
        return mSnackbarText.asObservable();
    }

    @NonNull
    public Observable<Boolean> getProgressIndicator() {
        return mProgressIndicatorSubject.asObservable();
    }
}
