/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.data

import com.example.android.architecture.blueprints.todoapp.data.source.local.FakeTaskDao
import com.example.android.architecture.blueprints.todoapp.data.source.local.LocalTask
import com.example.android.architecture.blueprints.todoapp.data.source.local.toExternal
import com.example.android.architecture.blueprints.todoapp.data.source.local.toLocal
import com.example.android.architecture.blueprints.todoapp.data.source.network.NetworkTask
import com.example.android.architecture.blueprints.todoapp.data.source.network.TaskNetworkDataSource
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultTaskRepositoryTest {

    private var testDispatcher = UnconfinedTestDispatcher()
    private var testScope = TestScope(testDispatcher)

    private val localTasks = listOf(
        LocalTask(id = "1", title = "title1", description = "description1", isCompleted = false),
        LocalTask(id = "2", title = "title2", description = "description2", isCompleted = true),
    )

    private val localDataSource = FakeTaskDao(localTasks)
    private val networkDataSource = TaskNetworkDataSource()
    private val taskRepository = DefaultTaskRepository(
        localDataSource = localDataSource,
        networkDataSource = networkDataSource,
        dispatcher = testDispatcher,
        scope = testScope
    )

    @Test
    fun observeAll_exposesLocalData() = runTest {
        val tasks = taskRepository.observeAll().first()
        assertEquals(localTasks.toExternal(), tasks)
    }

    @Test
    fun onTaskCreation_localAndNetworkAreUpdated() = testScope.runTest {
        val newTaskId = taskRepository.create(
            localTasks[0].title,
            localTasks[0].description
        )

        val localTasks = localDataSource.observeAll().first()
        assertEquals(true, localTasks.map { it.id }.contains(newTaskId))

        val networkTasks = networkDataSource.loadTasks()
        assertEquals(true, networkTasks.map { it.id }.contains(newTaskId))
    }

    @Test
    fun onTaskCompletion_localAndNetworkAreUpdated() = testScope.runTest {
        taskRepository.complete("1")

        val localTasks = localDataSource.observeAll().first()
        val isLocalTaskComplete = localTasks.firstOrNull { it.id == "1" } ?.isCompleted
        assertEquals(true, isLocalTaskComplete)

        val networkTasks = networkDataSource.loadTasks()
        val isNetworkTaskComplete =
            networkTasks.firstOrNull { it.id == "1"} ?.status == NetworkTask.TaskStatus.COMPLETE
        assertEquals(true, isNetworkTaskComplete)
    }

    @Test
    fun onRefresh_localIsEqualToNetwork() = runTest {
        val networkTasks = listOf(
            NetworkTask(id = "3", title = "title3", shortDescription = "desc3"),
            NetworkTask(id = "4", title = "title4", shortDescription = "desc4"),
        )
        networkDataSource.saveTasks(networkTasks)

        taskRepository.refresh()

        assertEquals(networkTasks.toLocal(), localDataSource.observeAll().first())
    }
}
