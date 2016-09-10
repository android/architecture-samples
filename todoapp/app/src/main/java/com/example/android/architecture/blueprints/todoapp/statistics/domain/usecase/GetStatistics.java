package com.example.android.architecture.blueprints.todoapp.statistics.domain.usecase;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.Subscription;
import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.model.Statistics;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Calculate statistics of active and completed Tasks {@link Task} in the {@link TasksRepository}.
 */
public class GetStatistics implements UseCase<GetStatistics.RequestValues, GetStatistics.ResponseValue> {

    private final TasksRepository mTasksRepository;

    public GetStatistics(@NonNull TasksRepository tasksRepository) {
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null!");
    }

    @Override
    public void executeUseCase(@NonNull RequestValues requestValues, @NonNull final Callback<ResponseValue> callback, @NonNull final Subscription subscription) {
        if (subscription.isUnsubscribed()) {
            return;
        }

        callback.onStart();

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

                if (subscription.isUnsubscribed()) {
                    return;
                }

                ResponseValue responseValue = new ResponseValue(new Statistics(completedTasks, activeTasks));
                callback.onNext(responseValue);
                callback.onCompleted();
            }

            @Override
            public void onDataNotAvailable() {
                if (subscription.isUnsubscribed()) {
                    return;
                }

                callback.onError(new Exception("Data not available"));
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
