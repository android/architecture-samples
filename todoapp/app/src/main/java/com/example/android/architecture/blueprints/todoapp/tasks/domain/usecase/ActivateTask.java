package com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase;

import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

public class ActivateTask extends UseCase<ActivateTask.RequestValues, ActivateTask.ResponseValue> {

    private final TasksRepository mTasksRepository;

    public ActivateTask(TasksRepository tasksRepository) {
        mTasksRepository = tasksRepository;
    }

    @Override
    protected void executeUseCase(final RequestValues values) {
        Task activeTask = values.getCompletedTask();
        mTasksRepository.activateTask(activeTask);
        getUseCaseCallback().onSuccess(new ResponseValue(activeTask));
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
