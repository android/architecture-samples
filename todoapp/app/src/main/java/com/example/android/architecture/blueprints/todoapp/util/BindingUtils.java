package com.example.android.architecture.blueprints.todoapp.util;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Priority;

public final class BindingUtils {


    public static int priorityToButtonId(Priority priority) {
        int selectedButtonId = -1;

        if (priority == null) {
            return selectedButtonId;
        }

        switch (priority) {
            case HIGH: {
                selectedButtonId = R.id.high;
                break;
            }
            case MEDIUM: {
                selectedButtonId = R.id.medium;
                break;
            }
            case LOW: {
                selectedButtonId = R.id.low;
                break;
            }
            case NONE: {
                selectedButtonId = R.id.none;
                break;
            }
        }
        return selectedButtonId;
    }


    public static Priority buttonIdToPriority(int selectedButtonId) {
        Priority priority = null;
        switch (selectedButtonId) {
            case R.id.high: {
                priority = Priority.HIGH;
                break;
            }
            case R.id.medium: {
                priority = Priority.MEDIUM;
                break;
            }

            case R.id.low: {
                priority = Priority.LOW;
                break;
            }

            case R.id.none: {
                priority = Priority.NONE;
                break;
            }
        }
        return priority;
    }
}
