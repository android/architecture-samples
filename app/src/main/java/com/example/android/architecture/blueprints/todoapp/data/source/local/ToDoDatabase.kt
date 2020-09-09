/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.android.architecture.blueprints.todoapp.data.PriorityConverter
import com.example.android.architecture.blueprints.todoapp.data.Task


/**
 * The Room Database that contains the Task table.
 *
 * Note that exportSchema should be true in production databases.
 */
@Database(entities = [Task::class], version = 2, exportSchema = false)
@TypeConverters(PriorityConverter::class)
abstract class ToDoDatabase : RoomDatabase() {

    abstract fun taskDao(): TasksDao
}

/**
 * This migration can be used for upgrades since the new Room DB for Tasks has a
 * new field called "priority"
 */
val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE tasks "
                + " ADD COLUMN priority INTEGER NOT NULL DEFAULT 1")
    }
}