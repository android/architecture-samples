/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Comparator;
import java.util.UUID;

/**
 * Immutable model class for a Task.
 */
@Entity(tableName = "tasks")
public final class Task {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "entryid")
    private final String mId;

    @Nullable
    @ColumnInfo(name = "title")
    private final String mTitle;

    @Nullable
    @ColumnInfo(name = "description")
    private final String mDescription;

    @ColumnInfo(name = "completed")
    private final boolean mCompleted;

    @ColumnInfo(name = "priority")
    private final Priority mPriority;

    /**
     * Use this constructor to create a new active Task.
     *
     * @param title       title of the task
     * @param description description of the task
     */
    @Ignore
    public Task(@Nullable String title, @Nullable String description, @Nullable Priority priority) {
        this(title, description, priority, UUID.randomUUID().toString(), false);
    }

    /**
     * Use this constructor to create an active Task if the Task already has an id (copy of another
     * Task).
     *
     * @param title       title of the task
     * @param description description of the task
     * @param id          id of the task
     */
    @Ignore
    public Task(@Nullable String title, @Nullable String description, @Nullable Priority priority, @NonNull String id) {
        this(title, description, priority, id, false);
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @Nullable
    public String getTitle() {
        return mTitle;
    }

    /**
     * Use this constructor to create a new completed Task.
     *
     * @param title       title of the task
     * @param description description of the task
     * @param completed   true if the task is completed, false if it's active
     */
    @Ignore
    public Task(@Nullable String title, @Nullable String description, @Nullable Priority priority, boolean completed) {
        this(title, description, priority, UUID.randomUUID().toString(), completed);
    }

    @Nullable
    public String getTitleForList() {
        if (mTitle != null && !mTitle.isEmpty()) {
            return mTitle;
        } else {
            return mDescription;
        }
    }

    @Nullable
    public String getDescription() {
        return mDescription;
    }

    public boolean isCompleted() {
        return mCompleted;
    }

    public boolean isActive() {
        return !mCompleted;
    }

    public boolean isEmpty() {
        return (mTitle == null || mTitle.isEmpty()) && (mDescription == null || mDescription.isEmpty());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return ObjectsCompat.equals(mId, task.mId) &&
                ObjectsCompat.equals(mTitle, task.mTitle) &&
                ObjectsCompat.equals(mDescription, task.mDescription);
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(mId, mTitle, mDescription);
    }

    @Override
    public String toString() {
        return "Task with title " + mTitle;
    }

    /**
     * Use this constructor to specify a completed Task if the Task already has an id (copy of
     * another Task).
     *
     * @param title       title of the task
     * @param description description of the task
     * @param id          id of the task
     * @param completed   true if the task is completed, false if it's active
     */
    public Task(@Nullable String title, @Nullable String description, @Nullable Priority priority,
                @NonNull String id, boolean completed) {
        mId = id;
        mTitle = title;
        mDescription = description;
        mCompleted = completed;
        mPriority = priority;
    }

    @Nullable
    public Priority getPriority() {
        return mPriority;
    }

    public static Comparator<Task> nameComparator = new Comparator<Task>() {
        @Override
        public int compare(Task o1, Task o2) {
            if (o1 == null || o2 == null) {
                return -1;
            } else {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        }
    };
    public static Comparator<Task> PRIORITY_COMPARATOR = new Comparator<Task>() {
        @Override
        public int compare(Task o1, Task o2) {

            if (o1 == null || o2 == null) {
                return -1;
            } else {
                if (o1.getPriority() == o2.getPriority()) {
                    return o1.getTitle().compareTo(o2.getTitle());
                } else {
                    return o1.getPriority().compareTo(o2.getPriority());
                }
            }
        }
    };
}
