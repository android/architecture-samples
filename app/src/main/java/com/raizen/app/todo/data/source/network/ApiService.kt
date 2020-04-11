package com.raizen.app.todo.data.source.network

import io.reactivex.Single
import retrofit2.http.GET

interface ApiService {


    @GET("/posts")
    fun getTasks(): Single<List<TaskResponse>>
}