package com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase;

import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

public class SaveTask extends UseCase<SaveTask.RequestValues, SaveTask.ResponseValue> {

    private final TasksRepository mTasksRepository;

    public SaveTask(TasksRepository tasksRepository) {
        mTasksRepository = tasksRepository;
    }

    @Override
    protected void executeUseCase(final RequestValues values) {
        Task task = values.getTask();
        mTasksRepository.saveTask(task);

        getUseCaseCallback().onSuccess(new ResponseValue(task));
    }

    public static class RequestValues extends UseCase.RequestValues {
        private final Task mTask;

        public RequestValues(Task task) {
            mTask = task;
        }

        public Task getTask() {
            return mTask;
        }
    }

    public class ResponseValue extends UseCase.ResponseValue {
        private Task mTask;

        public ResponseValue(Task task) {
            mTask = task;
        }

        public Task getTask() {
            return mTask;
        }
    }
}
