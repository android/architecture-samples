package com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase;

import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

public class ActivateTask extends UseCase<ActivateTask.RequestValues, ActivateTask.ResponseValue> {

    private final TasksRepository mTasksRepository;

    public ActivateTask(TasksRepository tasksRepository) {
        mTasksRepository = tasksRepository;
    }

    @Override
    protected void executeUseCase(final RequestValues values) {
        String activeTask = values.getActivateTask();
        mTasksRepository.activateTask(activeTask);
        getUseCaseCallback().onSuccess(new ResponseValue());
    }

    public static class RequestValues extends UseCase.RequestValues {

        private final String mActivateTask;

        public RequestValues(String activateTask) {
            mActivateTask = activateTask;
        }

        public String getActivateTask() {
            return mActivateTask;
        }
    }

    public class ResponseValue extends UseCase.ResponseValue {
    }
}
