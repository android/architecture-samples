package com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase;

import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

public class ClearCompleteTasks extends UseCase<ClearCompleteTasks.RequestValues, ClearCompleteTasks.ResponseValue> {

    private final TasksRepository mTasksRepository;

    public ClearCompleteTasks(TasksRepository tasksRepository) {
        mTasksRepository = tasksRepository;
    }

    @Override
    protected void executeUseCase(final RequestValues values) {
        mTasksRepository.clearCompletedTasks();
        getUseCaseCallback().onSuccess(new ResponseValue());
    }

    public static class RequestValues extends UseCase.RequestValues {}

    public class ResponseValue extends UseCase.ResponseValue {}
}
