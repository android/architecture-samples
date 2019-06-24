package com.example.android.architecture.blueprints.todoapp

import androidx.fragment.app.Fragment
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
