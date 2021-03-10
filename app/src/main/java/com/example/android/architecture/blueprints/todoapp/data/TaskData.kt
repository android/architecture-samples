package com.example.android.architecture.blueprints.todoapp.data

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Job

class TaskData(
        val job: Job,
        val countdownTimer: MutableLiveData<Int>
)