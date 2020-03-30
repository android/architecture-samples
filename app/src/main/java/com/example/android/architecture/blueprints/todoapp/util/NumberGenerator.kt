package com.example.android.architecture.blueprints.todoapp.util

import android.content.Context
import android.net.ConnectivityManager
import java.util.*
object NumberGenerator {
    public fun generator(): String {
        val random = Random()
        val generatedPassword = java.lang.String.format("%04d", random.nextInt(10000))
        return generatedPassword
    }


}