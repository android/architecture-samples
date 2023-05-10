/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.google.samples.modularization.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultMyModelRepository @Inject constructor() : MyModelRepository {

    private val allItems: MutableStateFlow<List<MyModel>> = MutableStateFlow(
        listOf(
            MyModel(
                id = 1,
                title = "Item 1",
                description = "Description",
                timestamp = 1672368617954,
                isBookmarked = false
            ),
            MyModel(
                id = 2,
                title = "Item 2",
                description = "Description",
                timestamp = 1664678230741,
                isBookmarked = false
            ),
            MyModel(
                id = 3,
                title = "Item 3",
                description = "Description",
                timestamp = 1667884312189,
                isBookmarked = false
            )
        )
    )

    override val observeAllModels: Flow<List<MyModel>> = allItems

    override fun observeModelById(id: Long): Flow<MyModel> = observeAllModels.map { items ->
        items.firstOrNull { model -> model.id == id }
            ?: throw NoSuchElementException("$id not found")
    }

    override suspend fun bookmark(id: Long, isBookmarked: Boolean) {
        allItems.getAndUpdate { items ->
            items.map { model ->
                if(model.id == id) {
                    model.copy(isBookmarked = isBookmarked)
                } else {
                    model
                }
            }
        }
    }
}
