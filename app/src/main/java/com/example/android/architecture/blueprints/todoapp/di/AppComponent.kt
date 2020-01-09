package com.example.android.architecture.blueprints.todoapp.di

import android.content.Context
import com.example.android.architecture.blueprints.todoapp.addedittask.di.AddEditTaskComponent
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.statistics.di.StatisticsComponent
import com.example.android.architecture.blueprints.todoapp.taskdetail.di.TaskDetailComponent
import com.example.android.architecture.blueprints.todoapp.tasks.di.TasksComponent
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import javax.inject.Singleton

/**
 * Main component for the application.
 *
 * See the `TestApplicationComponent` used in UI tests.
 */
@Singleton
@Component(
    modules = [
        AppModule::class,
        AppModuleBinds::class,
        ViewModelBuilderModule::class,
        SubcomponentsModule::class
    ]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context): AppComponent
    }

    fun addEditTaskComponent(): AddEditTaskComponent.Factory
    fun statisticsComponent(): StatisticsComponent.Factory
    fun taskDetailComponent(): TaskDetailComponent.Factory
    fun tasksComponent(): TasksComponent.Factory

    val tasksRepository: TasksRepository
}

@Module(
    subcomponents = [
        TasksComponent::class,
        AddEditTaskComponent::class,
        StatisticsComponent::class,
        TaskDetailComponent::class
    ]
)
object SubcomponentsModule