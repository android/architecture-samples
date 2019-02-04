/*
 *  Copyright 2017 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.databinding.TaskItemBinding


class TasksAdapter(
        private var tasks: List<Task>,
        private val tasksViewModel: TasksViewModel
) : BaseAdapter() {

    fun replaceData(tasks: List<Task>) {
        setList(tasks)
    }

    override fun getCount() = tasks.size

    override fun getItem(position: Int) = tasks[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup): View {
        val binding: TaskItemBinding
        if (view == null) {
            // Inflate
            val inflater = LayoutInflater.from(viewGroup.context)

            // Create the binding
            binding = TaskItemBinding.inflate(inflater, viewGroup, false)
        } else {
            // Recycling view
            binding = DataBindingUtil.getBinding<TaskItemBinding>(view)
        }

        val userActionsListener = object : TaskItemUserActionsListener {
            override fun onCompleteChanged(task: Task, v: View) {
                val checked = (v as CheckBox).isChecked
                tasksViewModel.completeTask(task, checked)
            }

            override fun onTaskClicked(task: Task) {
                tasksViewModel.openTaskEvent.value = task.id
            }
        }

        with(binding) {
            task = tasks[position]
            listener = userActionsListener
            executePendingBindings()
        }

        return binding.root
    }


    private fun setList(tasks: List<Task>) {
        this.tasks = tasks
        notifyDataSetChanged()
    }
}
