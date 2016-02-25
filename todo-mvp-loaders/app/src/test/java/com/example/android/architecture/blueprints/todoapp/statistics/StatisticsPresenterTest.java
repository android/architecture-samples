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

import android.support.v4.content.Loader;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksLoader;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link StatisticsPresenter}
 */
public class StatisticsPresenterTest {

    private static List<Task> TASKS;

    @Mock
    private StatisticsContract.View mStatisticsView;

    @Mock
    private TasksLoader mTasksLoader;

    private StatisticsPresenter mStatisticsPresenter;

    @Before
    public void setupStatisticsPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mStatisticsPresenter = new StatisticsPresenter(mStatisticsView, mTasksLoader);

        // We initialise the tasks to 3, with one active and two completed
        TASKS = Lists.newArrayList(new Task("Title1", "Description1"),
                new Task("Title2", "Description2", true), new Task("Title3", "Description3", true));
    }

    @Test
    public void loadEmptyTasksFromRepository_CallViewToDisplay() {
        // When the loader finishes with no tasks
        TASKS.clear();
        mStatisticsPresenter.onLoadFinished(mock(Loader.class), TASKS);

        // Then progress indicator is hidden and correct data is passed on to the view
        verify(mStatisticsView).setProgressIndicator(false);
        verify(mStatisticsView).displayStatistics(0, 0);
    }

    @Test
    public void loadNonEmptyTasksFromRepository_CallViewToDisplay() {
        // When the loader finishes with tasks
        mStatisticsPresenter.onLoadFinished(mock(Loader.class), TASKS);

        // Then progress indicator is hidden and correct data is passed on to the view
        verify(mStatisticsView).setProgressIndicator(false);
        verify(mStatisticsView).displayStatistics(1, 2);
    }

    @Test
    public void loadStatisticsWhenTasksAreUnavailable_CallErrorToDisplay() {
        // When the loader returns null
        mStatisticsPresenter.onLoadFinished(mock(Loader.class), null);

        // Then an error message is shown
        verify(mStatisticsView).showLoadingStatisticsError();
    }
}
