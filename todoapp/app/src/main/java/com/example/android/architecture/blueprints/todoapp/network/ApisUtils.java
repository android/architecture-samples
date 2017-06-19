package com.example.android.architecture.blueprints.todoapp.network;

/**
 * Created by Sa7r on 6/18/2017.
 */

public class ApisUtils {
    private static String BASE_URL="https://jsonplaceholder.typicode.com/";

    public static GetFaqsApi getFaqsAPIInstance() {

        return RetrofitNetworkManager.getClient(BASE_URL).create(GetFaqsApi.class);
    }
}
