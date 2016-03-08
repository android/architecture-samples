package com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase;

import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

public class CompleteTask extends UseCase<CompleteTask.RequestValues, CompleteTask.ResponseValue> {

    private final TasksRepository mTasksRepository;

    public CompleteTask(TasksRepository tasksRepository) {
        mTasksRepository = tasksRepository;
    }

    @Override
    protected void executeUseCase(final RequestValues values) {
        Task completedTask = values.getCompletedTask();
        mTasksRepository.completeTask(completedTask);
        getUseCaseCallback().onSuccess(new ResponseValue(completedTask));
    }

    public static class RequestValues extends UseCase.RequestValues {

        private final Task mCompletedTask;

        public RequestValues(Task completedTask) {
            mCompletedTask = completedTask;
        }

        public Task getCompletedTask() {
            return mCompletedTask;
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
