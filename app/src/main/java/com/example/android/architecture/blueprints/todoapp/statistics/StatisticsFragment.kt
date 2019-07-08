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
package com.example.android.architecture.blueprints.todoapp.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.databinding.StatisticsFragBinding
import com.example.android.architecture.blueprints.todoapp.util.setupRefreshLayout
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Main UI for the statistics screen.
 */
class StatisticsFragment : DaggerFragment() {

    private lateinit var viewDataBinding: StatisticsFragBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val statisticsViewModel by viewModels<StatisticsViewModel> { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = DataBindingUtil.inflate(
            inflater, R.layout.statistics_frag, container,
            false
        )
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewDataBinding.viewmodel = statisticsViewModel
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        this.setupRefreshLayout(viewDataBinding.refreshLayout)
        statisticsViewModel.start()
    }
}
