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

package com.example.android.architecture.blueprints.todoapp.data.source.network

/**
 * Main entry point for accessing tasks data from the network.
 *
 */
interface NetworkDataSource {

    /**
     * Load list of Tasks from network
     * @return List<NeNetworkTask>
     */
    suspend fun loadTasks(): List<NetworkTask>

    /**
     * Save list of Tasks to network
     * @param List<NetworkTask>
     */
    suspend fun saveTasks(newTasks: List<NetworkTask>)
}
