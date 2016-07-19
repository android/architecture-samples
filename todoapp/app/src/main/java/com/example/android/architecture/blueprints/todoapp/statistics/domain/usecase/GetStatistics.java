package com.example.android.architecture.blueprints.todoapp.statistics.domain.usecase;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.model.Statistics;

import java.util.List;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Calculate statistics of active and completed Tasks {@link Task} in the {@link TasksRepository}.
 */
public class GetStatistics extends UseCase<GetStatistics.RequestValues, GetStatistics.ResponseValue> {

    private final TasksRepository mTasksRepository;

    @Inject public GetStatistics(@NonNull TasksRepository tasksRepository) {
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null!");
    }

    @Override
    protected void executeUseCase(RequestValues requestValues) {
        mTasksRepository.getTasks(new TasksDataSource.LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {

                int activeTasks = 0;
                int completedTasks = 0;

                // We calculate number of active and completed tasks
                for (Task task : tasks) {
                    if (task.isCompleted()) {
                        completedTasks += 1;
                    } else {
                        activeTasks += 1;
                    }
                }

                ResponseValue responseValue = new ResponseValue(new Statistics(completedTasks, activeTasks));
                getUseCaseCallback().onSuccess(responseValue);
            }

            @Override
            public void onDataNotAvailable() {
                getUseCaseCallback().onError();
            }
        });
    }

    public static class RequestValues implements UseCase.RequestValues {
    }

    public static class ResponseValue implements UseCase.ResponseValue {

        private final Statistics mStatistics;

        public ResponseValue(@NonNull Statistics statistics) {
            mStatistics = checkNotNull(statistics, "statistics cannot be null!");
        }

        public Statistics getStatistics() {
            return mStatistics;
        }
    }
}
