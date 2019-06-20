package com.example.android.architecture.blueprints.todoapp

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.test.runner.AndroidJUnitRunner
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

/**
 * An application used from instrumentation tests. It has a fragment injector to enable
 * tests using FragmentScenario.
 */
class TestTodoApplication : TodoApplication(), HasSupportFragmentInjector {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector() = fragmentInjector
}

/**
 * A custom [AndroidJUnitRunner] used to replace the application used in tests with a
 * [TestTodoApplication].
 */
class CustomTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, TestTodoApplication::class.java.name, context)
    }
}