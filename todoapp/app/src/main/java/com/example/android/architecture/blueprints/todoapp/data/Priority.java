package com.example.android.architecture.blueprints.todoapp.data;

public enum Priority {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low"),
    NONE("None");


    private final String mPriority;

    Priority(String priority) {
        this.mPriority = priority;
    }

    public String getPriority() {
        return mPriority;
    }
}
