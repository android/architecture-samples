package com.example.android.architecture.blueprints.todoapp.data.source.remote

import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import io.reactivex.Observable
import android.os.Parcelable


import com.google.gson.annotations.SerializedName
import retrofit2.http.*


interface TodoService {
    @GET("/todo/users/{id}/tasks")
   suspend fun getTasks(@Path("id") id:String)
            : List<TasksModel>

    @GET("/todo/users/{user}/tasks/{id}")
    suspend fun getTask(@Path("user") user:String, @Path("id") id:String)
            : List<TasksModel>


    @Headers("Content-Type: application/json")
    @PUT("/todo/users/{user}/tasks")
    suspend fun createOrUpdate(@Path("user") user:String,
                               @Body task : List<TasksModel>): List<TasksModel>

    @DELETE("/todo/users/user345/tasks/{id}")
    suspend fun delete(@Path("id") id :String)
}


