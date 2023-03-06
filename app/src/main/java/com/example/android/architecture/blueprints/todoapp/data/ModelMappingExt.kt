/*
 * Copyright 2023 The Android Open Source Project
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

import com.example.android.architecture.blueprints.todoapp.data.source.local.LocalTask
import com.example.android.architecture.blueprints.todoapp.data.source.remote.NetworkTask
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TaskStatus

/**
 * Data model mapping extension functions. There are three model types:
 *
 * - Task: External model exposed to other layers in the architecture.
 * Obtained using `asExternalModel`.
 *
 * - NetworkTask: Internal model used to represent a task from the network. Obtained using
 * `asNetworkModel`.
 *
 * - LocalTask: Internal model used to represent a task stored locally in a database. Obtained
 * using `asLocalModel`.
 *
 */

// External to local
fun Task.asLocalModel() = LocalTask(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
)

fun List<Task>.asLocalModels() = map(Task::asLocalModel)

// Local to External
fun LocalTask.asExternalModel() = Task(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
)

// Note: JvmName is used to provide a unique name for each extension function with the same name.
// Without this, type erasure will cause compiler errors because these methods will have the same
// signature on the JVM.
@JvmName("taskEntitiesAsExternalModels")
fun List<LocalTask>.asExternalModels() = map(LocalTask::asExternalModel)

// Network to Local
fun NetworkTask.asTaskEntity() = LocalTask(
    id = id,
    title = title,
    description = shortDescription,
    isCompleted = (status == TaskStatus.COMPLETE),
)

@JvmName("networkTasksAsTaskEntities")
fun List<NetworkTask>.asTaskEntities() = map(NetworkTask::asTaskEntity)

// Local to Network
fun LocalTask.asNetworkModel() = NetworkTask(
    id = id,
    title = title,
    shortDescription = description,
    status = if (isCompleted) { TaskStatus.COMPLETE } else { TaskStatus.ACTIVE }
)

// External to Network
fun Task.asNetworkModel() = asLocalModel().asNetworkModel()

@JvmName("tasksAsNetworkTasks")
fun List<Task>.asNetworkModels() = map(Task::asNetworkModel)

// Network to External
fun NetworkTask.asExternalModel() = asTaskEntity().asExternalModel()

@JvmName("networkTasksAsTasks")
fun List<NetworkTask>.asExternalModels() = map(NetworkTask::asExternalModel)
