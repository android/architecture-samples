package com.example.android.architecture.blueprints.todoapp.di

import android.content.Context
import com.example.android.architecture.blueprints.todoapp.TestTodoApplication
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        TestAppModuleBinds::class,
        ViewModelBuilderModule::class,
        SubcomponentsModule::class
    ]
)
interface TestAppComponent : AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context): TestAppComponent
    }
}
