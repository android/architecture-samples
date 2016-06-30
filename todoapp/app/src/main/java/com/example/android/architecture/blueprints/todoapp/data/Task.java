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

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;
import com.google.common.base.Objects;

import java.util.UUID;

/**
 * Immutable model class for a Task.
 */
public final class Task {

    private final String mId;
    private final String mTitle;
    private final String mDescription;
    private final boolean mCompleted;
    private int mInternalId;

    /**
     * Use this constructor to create a new active Task.
     *
     * @param title
     * @param description
     */
    public Task(@Nullable String title, @Nullable String description) {
        mId = UUID.randomUUID().toString();
        mTitle = title;
        mDescription = description;
        mCompleted = false;
    }

    /**
     * Use this constructor to create an active Task if the Task already has an id (copy of another
     * Task).
     *
     * @param title
     * @param description
     * @param id of the class
     */
    public Task(@Nullable String title, @Nullable String description, String id) {
        mId = id;
        mTitle = title;
        mDescription = description;
        mCompleted = false;
    }

    /**
     * Use this constructor to create a new completed Task.
     *
     * @param title
     * @param description
     * @param completed
     */
    public Task(@Nullable String title, @Nullable String description, boolean completed) {
        mId = UUID.randomUUID().toString();
        mTitle = title;
        mDescription = description;
        mCompleted = completed;
    }

    /**
     * Use this constructor to specify a completed Task if the Task already has an id (copy of
     * another Task).
     *
     * @param title
     * @param description
     * @param id
     * @param completed
     */
    public Task(@Nullable String title, @Nullable String description, String id, boolean completed) {
        mId = id;
        mTitle = title;
        mDescription = description;
        mCompleted = completed;
    }

    /**
     * Use this constructor to return a Task from a Cursor
     *
     * @return
     */
    public static Task from(Cursor cursor) {
        String entryId = cursor.getString(cursor.getColumnIndexOrThrow(
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION));
        boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED)) == 1;
        return new Task(title, description, entryId, completed);
    }

    public static Task from(ContentValues values) {
        String entryId = values.getAsString(TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID);
        String title = values.getAsString(TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE);
        String description = values.getAsString(TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION);
        boolean completed = values.getAsInteger(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED) == 1;

        return new Task(title, description, entryId, completed);
    }

    public String getId() {
        return mId;
    }

    @Nullable
    public String getTitle() {
        return mTitle;
    }

    @Nullable
    public String getTitleForList() {
        if (mTitle != null && !mTitle.equals("")) {
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
        return (mTitle == null || "".equals(mTitle)) &&
                (mDescription == null || "".equals(mDescription));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        return Objects.equal(mId, task.mId) &&
                Objects.equal(mTitle, task.mTitle) &&
                Objects.equal(mDescription, task.mDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId, mTitle, mDescription);
    }

    @Override
    public String toString() {
        return "Task with title " + mTitle;
    }

}