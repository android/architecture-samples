package com.example.android.architecture.blueprints.todoapp.data.source.remote

import android.annotation.SuppressLint
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@SuppressLint("ParcelCreator")
data class TasksModel(
        @SerializedName("id")
        @Expose
        val  id : String?,
        val user: String,
        val title: String,
        val description: String,
        val completed: Boolean,
        val priority: String
)