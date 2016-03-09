package com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase;

import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

public class DeleteTask extends UseCase<DeleteTask.RequestValues, DeleteTask.ResponseValue> {

    private final TasksRepository mTasksRepository;

    public DeleteTask(TasksRepository tasksRepository) {
        mTasksRepository = tasksRepository;
    }

    @Override
    protected void executeUseCase(final RequestValues values) {
        mTasksRepository.deleteTask(values.getTaskId());
        getUseCaseCallback().onSuccess(new ResponseValue());
    }

    public static class RequestValues extends UseCase.RequestValues {
        private final String mTaskId;

        public RequestValues(String taskId) {
            mTaskId = taskId;
        }

        public String getTaskId() {
            return mTaskId;
        }
    }

    public class ResponseValue extends UseCase.ResponseValue {
    }
}
