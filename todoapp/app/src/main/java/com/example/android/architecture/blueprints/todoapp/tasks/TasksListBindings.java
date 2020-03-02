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

package com.example.android.architecture.blueprints.todoapp.tasks;

import android.widget.ListView;

import androidx.databinding.BindingAdapter;

import com.example.android.architecture.blueprints.todoapp.data.Task;

import java.util.List;

/**
 * Contains {@link BindingAdapter}s for the {@link Task} list.
 */
public class TasksListBindings {

    @SuppressWarnings("unchecked")
    @BindingAdapter("items")
    public static void setItems(ListView listView, List<Task> items) {
        TasksFragment.TasksAdapter adapter = (TasksFragment.TasksAdapter) listView.getAdapter();
        if (adapter != null)
        {
            adapter.replaceData(items);
        }
    }
}
