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
package com.example.android.architecture.blueprints.todoapp.taskdetail

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.android.architecture.blueprints.todoapp.EventObserver
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.databinding.TaskdetailFragBinding
import com.example.android.architecture.blueprints.todoapp.tasks.DELETE_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.util.getViewModelFactory
import com.example.android.architecture.blueprints.todoapp.util.setupRefreshLayout
import com.example.android.architecture.blueprints.todoapp.util.setupSnackbar
import com.google.android.material.snackbar.Snackbar

/**
 * Main UI for the task detail screen.
 */
class TaskDetailFragment : Fragment() {
	private lateinit var binding: TaskdetailFragBinding

	private val args: TaskDetailFragmentArgs by navArgs()

	private val viewModel by viewModels<TaskDetailViewModel> { getViewModelFactory() }

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		binding.editTaskButton.setOnClickListener { viewModel.editTask() }
		view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
		setupNavigation()
		this.setupRefreshLayout(binding.refreshLayout)
	}

	private fun setupNavigation() {
		viewModel.deleteTaskEvent.observe(this, EventObserver {
			val action = TaskDetailFragmentDirections
					.actionTaskDetailFragmentToTasksFragment(DELETE_RESULT_OK)
			findNavController().navigate(action)
		})
		viewModel.editTaskEvent.observe(this, EventObserver {
			val action = TaskDetailFragmentDirections
					.actionTaskDetailFragmentToAddEditTaskFragment(
							args.taskId,
							resources.getString(R.string.edit_task)
					)
			findNavController().navigate(action)
		})
	}

	override fun onCreateView(
			inflater: LayoutInflater,
			container: ViewGroup?,
			savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.taskdetail_frag, container, false)
		binding = TaskdetailFragBinding.bind(view)
		binding.viewmodel = viewModel
		binding.lifecycleOwner = this.viewLifecycleOwner

		viewModel.start(args.taskId)

		setHasOptionsMenu(true)
		return view
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.menu_delete -> {
				viewModel.deleteTask()
				true
			}
			else -> false
		}
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.taskdetail_frag_menu, menu)
	}
}
