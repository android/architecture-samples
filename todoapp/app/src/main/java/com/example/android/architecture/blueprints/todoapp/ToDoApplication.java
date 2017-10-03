package com.example.android.architecture.blueprints.todoapp;

import android.app.Application;
import android.os.StrictMode;

/**
 * Application class, used for setting the StrictMode.
 */
public class ToDoApplication extends Application {

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) {
            setStrictMode();
        }
        super.onCreate();
    }

    private void setStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }
}
