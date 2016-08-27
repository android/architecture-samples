package com.example.android.architecture.blueprints.todoapp.statistics;

import android.support.v4.util.Pair;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link StatisticsViewModel}
 */
public class StatisticsViewModelTest {

    private static List<Task> TASKS;

    @Mock
    private TasksRepository mTasksRepository;

    private StatisticsViewModel mViewModel;

    TestSubscriber<Boolean> mProgressIndicatorTestSubscriber;

    TestSubscriber<Pair<Integer, Integer>> mStatisticsTestSubscriber;

    @Before
    public void setupStatisticsPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mViewModel = new StatisticsViewModel(mTasksRepository);

        // We subscribe the tasks to 3, with one active and two completed
        TASKS = Lists.newArrayList(new Task("Title1", "Description1"),
                new Task("Title2", "Description2", true), new Task("Title3", "Description3", true));
        mProgressIndicatorTestSubscriber = new TestSubscriber<>();
        mStatisticsTestSubscriber = new TestSubscriber<>();
    }

    @Test
    public void getProgressIndicator_doesNotEmit_ifStatisticsNotRequested() {
        // When subscribing to the progress indicator
        mViewModel.getProgressIndicator().subscribe(mProgressIndicatorTestSubscriber);

        // No value is emitted
        mProgressIndicatorTestSubscriber.assertNoValues();
    }

    @Test
    public void getStatistics_withTasks_returnsCorrectData() {
        //Given a list of tasks in the repository
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(TASKS));

        //When subscribing to the statistics stream
        mViewModel.getStatistics().subscribe(mStatisticsTestSubscriber);

        //The correct pair is returned
        Pair<Integer, Integer> result = mStatisticsTestSubscriber.getOnNextEvents().get(0);
        assertThat(result.first, is(1));
        assertThat(result.second, is(2));
    }

    @Test
    public void getStatistics_withNoTasksreturnsCorrectData() {
        //Given a list of tasks in the repository
        when(mTasksRepository.getTasks()).thenReturn(Observable.<List<Task>>empty());

        //When subscribing to the statistics stream
        mViewModel.getStatistics().subscribe(mStatisticsTestSubscriber);

        //The correct pair is returned
        Pair<Integer, Integer> result = mStatisticsTestSubscriber.getOnNextEvents().get(0);
        assertThat(result.first, is(0));
        assertThat(result.second, is(0));
    }

    @Test
    public void getProgressIndicator_emits_afterStatisticsAreRetrieved() {
        //Given a list of tasks in the repository
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(TASKS));

        // And when subscribing to the progress indicator
        mViewModel.getProgressIndicator().subscribe(mProgressIndicatorTestSubscriber);
        //When subscribing to the statistics stream
        mViewModel.getStatistics().subscribe(mStatisticsTestSubscriber);

        // The values true and false were emitted
        mProgressIndicatorTestSubscriber.assertValues(true, false);
    }
}
