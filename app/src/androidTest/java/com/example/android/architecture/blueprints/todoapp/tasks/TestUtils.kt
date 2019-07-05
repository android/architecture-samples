package com.example.android.architecture.blueprints.todoapp.tasks

import android.app.Activity
import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ActivityScenario
import com.example.android.architecture.blueprints.todoapp.R

fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription()
    : String {
    var description = ""
    onActivity {
        description =
            it.findViewById<Toolbar>(R.id.toolbar).navigationContentDescription as String
    }
    return description
}