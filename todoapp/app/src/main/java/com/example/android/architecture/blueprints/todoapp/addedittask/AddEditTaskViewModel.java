package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.google.common.base.Strings;

import rx.Completable;
import rx.Observable;
import rx.subjects.PublishSubject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ViewModel handling the adding and deleting of tasks.
 */
public class AddEditTaskViewModel {

    @NonNull
    private final TasksRepository mTasksRepository;

    @NonNull
    private final AddEditTaskNavigator mNavigator;

    @NonNull
    private final PublishSubject<Integer> mSnackbarText;

    @Nullable
    private String mTaskId;

    @Nullable
    private String mRestoredTitle;

    @Nullable
    private String mRestoredDescription;

    public AddEditTaskViewModel(@Nullable String taskId, @NonNull TasksRepository tasksRepository,
                                @NonNull AddEditTaskNavigator navigator) {
        mTasksRepository = checkNotNull(tasksRepository, "TaskRepository cannot be null");
        mNavigator = checkNotNull(navigator, "navigator cannot be null");
        mTaskId = taskId;
        mSnackbarText = PublishSubject.create();
    }

    /**
     * @return a stream that emits when a snackbar should be displayed. The stream contains the
     * snackbar text
     */
    @NonNull
    public Observable<Integer> getSnackbarText() {
        return mSnackbarText.asObservable();
    }

    /**
     * @return a stream containing the task retrieved from the repository. An error will be emitted
     * if the task id is invalid.
     */
    @NonNull
    public Observable<Task> getTask() {
        if (Strings.isNullOrEmpty(mTaskId)) {
            // new task. nothing to do here.
            return Observable.empty();
        }
        return mTasksRepository
                .getTask(mTaskId)
                .map(this::restoreTask)
                .doOnError(__ -> showSnackbar(R.string.empty_task_message));
    }

    /**
     * Sets the restored state.
     *
     * @param title       the restored title.
     * @param description the restored description.
     */
    public void setRestoredState(@Nullable String title, @Nullable String description) {
        mRestoredTitle = title;
        mRestoredDescription = description;
    }

    private Task restoreTask(Task task) {
        String title = mRestoredTitle != null ? mRestoredTitle : task.getTitle();
        String description = mRestoredDescription != null ? mRestoredDescription : task.getDescription();

        return new Task(title, description, task.getId());
    }

    /**
     * Save a task (create if it's a new task or update if task exists) with a title and a description
     *
     * @param title       title of the task
     * @param description description of the task
     */
    public Completable saveTask(String title, String description) {
        return Completable.fromAction(() -> createUpdateTask(title, description));
    }

    private void createUpdateTask(String title, String description) {
        if (isNewTask()) {
            createTask(title, description);
        } else {
            updateTask(title, description);
        }
    }

    private boolean isNewTask() {
        return mTaskId == null;
    }

    private void createTask(String title, String description) {
        Task newTask = new Task(title, description);
        if (newTask.isEmpty()) {
            showSnackbar(R.string.empty_task_message);
        } else {
            mTasksRepository.saveTask(newTask);
            mNavigator.onTaskSaved();
        }
    }

    private void updateTask(String title, String description) {
        mTasksRepository.saveTask(new Task(title, description, mTaskId));
        mNavigator.onTaskSaved();
    }

    private void showSnackbar(@StringRes int textId) {
        mSnackbarText.onNext(textId);
    }
}
