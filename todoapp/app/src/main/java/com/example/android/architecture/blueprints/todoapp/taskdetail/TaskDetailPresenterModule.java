package com.example.android.architecture.blueprints.todoapp.taskdetail;

import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped;
import com.example.android.architecture.blueprints.todoapp.di.FragmentScoped;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

import static com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity.EXTRA_TASK_ID;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link TaskDetailPresenter}.
 */
@Module
public abstract class TaskDetailPresenterModule {


    @FragmentScoped
    @ContributesAndroidInjector
    abstract TaskDetailFragment taskDetailFragment();

    @ActivityScoped
    @Binds
    abstract TaskDetailContract.Presenter statitsticsPresenter(TaskDetailPresenter presenter);

    @Provides
    @ActivityScoped
    static String provideTaskId(TaskDetailActivity activity) {
        return activity.getIntent().getStringExtra(EXTRA_TASK_ID);
    }
}
