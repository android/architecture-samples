package com.example.android.architecture.blueprints.todoapp.network;

import com.example.android.architecture.blueprints.todoapp.faq.model.FAQModel;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Sa7r on 6/15/2017.
 */

public interface GetFaqsApi {
    @GET("posts")
    Observable<List<FAQModel>> listFAQs();
}
