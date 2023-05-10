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

package com.google.samples.modularization.data

import com.google.samples.modularization.core.data.DefaultMyModelRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test


class DefaultMyModelRepositoryTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `ensure item is bookmarked`() = runTest {
        val repository = DefaultMyModelRepository()
        val items = repository.observeAllModels.first()
        assert(repository.observeAllModels.first().none { item -> item.isBookmarked })
        val firstItemId = items.first().id
        repository.bookmark(firstItemId, true)
        val firstItem = repository.observeModelById(firstItemId).first()
        assert(firstItem.isBookmarked)
    }
}
