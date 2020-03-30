package com.example.android.architecture.blueprints.todoapp.data.source.remote

import com.google.gson.Gson
//import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object TodoServiceFactory {

    fun makeGithubTrendingService(isDebug: Boolean): TodoService {
        val okHttpClient = makeOkHttpClient(
                makeLoggingInterceptor((isDebug)))
        return makeGithubTrendingService(okHttpClient, Gson())
    }

    private fun makeGithubTrendingService(okHttpClient: OkHttpClient, gson: Gson): TodoService {
        val retrofit = Retrofit.Builder()
                .baseUrl("http://tunein-android-challenge.herokuapp.com")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        return retrofit.create(TodoService::class.java)
    }

    private fun makeOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build()
    }

    private fun makeLoggingInterceptor(isDebug: Boolean): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = if (true) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        return logging
    }
}