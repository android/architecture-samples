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

package com.example.android.architecture.blueprints.todoapp.statistics;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.databinding.StatisticsFragBinding;

/**
 * Main UI for the statistics screen.
 */
public class StatisticsFragment extends Fragment {

    private StatisticsFragBinding mViewDataBinding;

    private StatisticsViewModel mStatisticsViewModel;

    public static StatisticsFragment newInstance() {
        return new StatisticsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mViewDataBinding = DataBindingUtil.inflate(
                inflater, R.layout.statistics_frag, container, false);
        return mViewDataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mStatisticsViewModel = StatisticsActivity.obtainViewModel(getActivity());
        mViewDataBinding.setStats(mStatisticsViewModel);
    }

    @Override
    public void onResume() {
        super.onResume();
        mStatisticsViewModel.start();
    }

    public boolean isActive() {
        return isAdded();
    }
}
