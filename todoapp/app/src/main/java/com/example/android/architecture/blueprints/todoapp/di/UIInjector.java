package com.example.android.architecture.blueprints.todoapp.di;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.android.AndroidInjection;
import dagger.android.support.AndroidSupportInjection;
import dagger.android.support.HasSupportFragmentInjector;
//UIInjector will automatically register our activities and fragments with Dagger
@Singleton
public class UIInjector {

    @Inject
    public UIInjector() {
    }

    public void inject(Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                handleActivity(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    private void handleActivity(Activity activity) {
        if (activity instanceof HasSupportFragmentInjector) {
            // this works because we're using support fragments, so it's okay to inject after Activity.super.onCreate()
            // See https://github.com/googlesamples/android-architecture-components/issues/99
            AndroidInjection.inject(activity);
        }

        if (activity instanceof FragmentActivity) {
            FragmentActivity fragmentActivity = (FragmentActivity) activity;
            fragmentActivity.getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager
                    .FragmentLifecycleCallbacks() {
                @Override
                public void onFragmentPreAttached(FragmentManager fm, Fragment f, Context context) {
                    if (f instanceof Injectable) {
                        AndroidSupportInjection.inject(f);
                    }
                }
            }, true);
        }
    }
}
