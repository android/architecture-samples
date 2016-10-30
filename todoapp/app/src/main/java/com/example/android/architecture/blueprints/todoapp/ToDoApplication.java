package com.example.android.architecture.blueprints.todoapp;

import android.app.Application;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskComponent;
import com.example.android.architecture.blueprints.todoapp.data.source.DaggerTasksRepositoryComponent;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepositoryComponent;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsComponent;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailComponent;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksComponent;

/**
 * Even though Dagger2 allows annotating a {@link dagger.Component} as a singleton, the code itself
 * must ensure only one instance of the class is created. Therefore, we create a custom
 * {@link Application} class to store a singleton reference to the {@link
 * TasksRepositoryComponent}.
 * <P>
 * The application is made of 5 Dagger components, as follows:<BR />
 * {@link TasksRepositoryComponent}: the data (it encapsulates a db and server data)<BR />
 * {@link TasksComponent}: showing the list of to do items, including marking them as
 * completed<BR />
 * {@link AddEditTaskComponent}: adding or editing a to do item<BR />
 * {@link TaskDetailComponent}: viewing details about a to do item, inlcuding marking it as
 * completed and deleting it<BR />
 * {@link StatisticsComponent}: viewing statistics about your to do items<BR />
 */
public class ToDoApplication extends Application {

    private TasksRepositoryComponent mRepositoryComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mRepositoryComponent = DaggerTasksRepositoryComponent.builder()
                .applicationModule(new ApplicationModule((getApplicationContext())))
                .build();

    }

    public TasksRepositoryComponent getTasksRepositoryComponent() {
        return mRepositoryComponent;
    }

}
