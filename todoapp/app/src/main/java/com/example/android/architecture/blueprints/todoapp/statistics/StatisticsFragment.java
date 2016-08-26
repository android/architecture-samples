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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.Injection;
import com.example.android.architecture.blueprints.todoapp.R;

import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

/**
 * Main UI for the statistics screen.
 */
public class StatisticsFragment extends Fragment {

    private TextView mStatisticsTV;

    @NonNull
    private StatisticsViewModel mViewModel;

    @NonNull
    private CompositeSubscription mSubscription;


    public static StatisticsFragment newInstance() {
        return new StatisticsFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = Injection.provideStatisticsViewModel(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.statistics_frag, container, false);
        mStatisticsTV = (TextView) root.findViewById(R.id.statistics);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        bind();
    }

    @Override
    public void onPause() {
        super.onPause();
        unbind();
    }

    private void bind() {
        mSubscription = new CompositeSubscription();

        mSubscription.add(mViewModel.getProgressIndicator()
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        showLoadingStatisticsError();
                    }

                    @Override
                    public void onNext(Boolean active) {
                        setProgressIndicator(active);
                    }
                }));

        mSubscription.add(mViewModel.getStatistics()
                .subscribe(new Subscriber<Pair<Integer, Integer>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        showLoadingStatisticsError();
                    }

                    @Override
                    public void onNext(Pair<Integer, Integer> activeCompletedTasks) {
                        showStatistics(activeCompletedTasks.first,
                                activeCompletedTasks.second);
                    }
                }));

    }

    private void unbind() {
        mSubscription.unsubscribe();
    }

    private void setProgressIndicator(boolean active) {
        if (active) {
            mStatisticsTV.setText(getString(R.string.loading));
        }
    }

    private void showStatistics(int numberOfIncompleteTasks, int numberOfCompletedTasks) {
        if (numberOfCompletedTasks == 0 && numberOfIncompleteTasks == 0) {
            mStatisticsTV.setText(getResources().getString(R.string.statistics_no_tasks));
        } else {
            String displayString = getResources().getString(R.string.statistics_active_tasks) + " "
                    + numberOfIncompleteTasks + "\n" + getResources().getString(
                    R.string.statistics_completed_tasks) + " " + numberOfCompletedTasks;
            mStatisticsTV.setText(displayString);
        }
    }

    private void showLoadingStatisticsError() {
        mStatisticsTV.setText(getResources().getString(R.string.statistics_error));
    }

}
