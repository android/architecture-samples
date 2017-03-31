package com.example.android.architecture.blueprints.todoapp;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;

/**
 * Used to track whether an Activity has been destroyed.
 * <p>
 * You can bypass this activity if you target 17+.
 */
public class BaseActivity  extends AppCompatActivity {

    private boolean mIsDestroyed = false;

    @Override
    protected void onDestroy() {
        mIsDestroyed = true;
        super.onDestroy();
    }

    @Override
    public boolean isDestroyed() {

        if (Build.VERSION.SDK_INT >= 17) {
            return super.isDestroyed();
        } else {
            return mIsDestroyed;
        }
    }
}
