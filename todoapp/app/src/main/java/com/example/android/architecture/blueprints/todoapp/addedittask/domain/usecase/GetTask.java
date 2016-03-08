package com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase;

import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.base.domain.error.DataNotAvailableError;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

public class GetTask extends UseCase<GetTask.RequestValues, GetTask.ResponseValue> {

    private final TasksRepository mTasksRepository;

    public GetTask(TasksRepository tasksRepository) {
        mTasksRepository = tasksRepository;
    }

    @Override
    protected void executeUseCase(final RequestValues values) {
        mTasksRepository.getTask(values.getTaskId(), new TasksDataSource.GetTaskCallback() {
            @Override
            public void onTaskLoaded(Task task) {
                ResponseValue responseValue = new ResponseValue(task);
                getUseCaseCallback().onSuccess(responseValue);
            }

            @Override
            public void onDataNotAvailable() {
                getUseCaseCallback().onError(new DataNotAvailableError());
            }
        });
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
        private Task mTask;

        public ResponseValue(Task task) {
            mTask = task;
        }

        public Task getTask() {
            return mTask;
        }
    }
}
