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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.Injection;
import com.example.android.architecture.blueprints.todoapp.R;
import com.google.common.base.Preconditions;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Main UI for the statistics screen.
 */
public class StatisticsFragment extends Fragment {

    @Nullable
    private TextView mStatisticsTV;

    @Nullable
    private StatisticsViewModel mViewModel;

    @Nullable
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
        bindViewModel();
    }

    @Override
    public void onPause() {
        unbindViewModel();
        super.onPause();
    }

    private void bindViewModel() {
        Preconditions.checkNotNull(mViewModel);

        mSubscription = new CompositeSubscription();

        mSubscription.add(mViewModel.getProgressIndicator()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean active) {
                        setProgressIndicator(active);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showLoadingStatisticsError();
                    }
                }));

        mSubscription.add(mViewModel.getStatistics()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String statistics) {
                        showStatistics(statistics);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showLoadingStatisticsError();
                    }
                }));
    }

    private void unbindViewModel() {
        Preconditions.checkNotNull(mSubscription);

        mSubscription.unsubscribe();
    }

    private void setProgressIndicator(boolean active) {
        if (active) {
            getStatisticsTextView().setText(getString(R.string.loading));
        }
    }

    private void showStatistics(@NonNull String statistics) {
        getStatisticsTextView().setText(statistics);
    }

    private void showLoadingStatisticsError() {
        getStatisticsTextView().setText(getResources().getString(R.string.statistics_error));
    }

    @NonNull
    private TextView getStatisticsTextView() {
        return Preconditions.checkNotNull(mStatisticsTV);
    }
}
