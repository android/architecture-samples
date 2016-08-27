package com.example.android.architecture.blueprints.todoapp.statistics;

<<<<<<< 9cd059efaef0fee63c4205745cd4f6c33cc5d24b
import android.support.annotation.StringRes;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.providers.BaseResourceProvider;
=======
import android.support.v4.util.Pair;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
>>>>>>> StatisticsViewModel tests added
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

<<<<<<< 9cd059efaef0fee63c4205745cd4f6c33cc5d24b
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
=======
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
>>>>>>> StatisticsViewModel tests added
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link StatisticsViewModel}
 */
public class StatisticsViewModelTest {

<<<<<<< 9cd059efaef0fee63c4205745cd4f6c33cc5d24b
    private static final String NO_TASKS = "no tasks";
    private static final String ACTIVE_COMPLETED_TASKS = "Active tasks: %1$d \n Completed tasks: %2$d";

    private List<Task> mTasks;
=======
    private static List<Task> TASKS;
>>>>>>> StatisticsViewModel tests added

    @Mock
    private TasksRepository mTasksRepository;

<<<<<<< 9cd059efaef0fee63c4205745cd4f6c33cc5d24b
    @Mock
    private BaseResourceProvider mResourceProvider;

    private StatisticsViewModel mViewModel;

    private TestSubscriber<Boolean> mProgressIndicatorTestSubscriber;

    private TestSubscriber<String> mStatisticsTestSubscriber;
=======
    private StatisticsViewModel mViewModel;

    TestSubscriber<Boolean> mProgressIndicatorTestSubscriber;

    TestSubscriber<Pair<Integer, Integer>> mStatisticsTestSubscriber;
>>>>>>> StatisticsViewModel tests added

    @Before
    public void setupStatisticsPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
<<<<<<< 9cd059efaef0fee63c4205745cd4f6c33cc5d24b
        mViewModel = new StatisticsViewModel(mTasksRepository, mResourceProvider);

        // We subscribe the tasks to 3, with one active and two completed
        mTasks = Lists.newArrayList(
                new Task("Title1", "Description1"),
                new Task("Title2", "Description2", true),
                new Task("Title3", "Description3", true));
=======
        mViewModel = new StatisticsViewModel(mTasksRepository);

        // We subscribe the tasks to 3, with one active and two completed
        TASKS = Lists.newArrayList(new Task("Title1", "Description1"),
                new Task("Title2", "Description2", true), new Task("Title3", "Description3", true));
>>>>>>> StatisticsViewModel tests added
        mProgressIndicatorTestSubscriber = new TestSubscriber<>();
        mStatisticsTestSubscriber = new TestSubscriber<>();
    }

    @Test
<<<<<<< 9cd059efaef0fee63c4205745cd4f6c33cc5d24b
    public void getProgressIndicator_emitsFalseInitially() {
        // When subscribing to the progress indicator
        mViewModel.getProgressIndicator().subscribe(mProgressIndicatorTestSubscriber);

        // One value: false, is emitted
        mProgressIndicatorTestSubscriber.assertValue(false);
=======
    public void getProgressIndicator_doesNotEmit_ifStatisticsNotRequested() {
        // When subscribing to the progress indicator
        mViewModel.getProgressIndicator().subscribe(mProgressIndicatorTestSubscriber);

        // No value is emitted
        mProgressIndicatorTestSubscriber.assertNoValues();
>>>>>>> StatisticsViewModel tests added
    }

    @Test
    public void getStatistics_withTasks_returnsCorrectData() {
        //Given a list of tasks in the repository
<<<<<<< 9cd059efaef0fee63c4205745cd4f6c33cc5d24b
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(mTasks));

        //When subscribing to the statistics stream
        mViewModel.getStatistics().subscribe();

        //The correct pair is returned
        verify(mResourceProvider).getString(R.string.statistics_active_completed_tasks, 1, 2);
    }

    @Test
    public void getStatistics_withNoTasks_returnsCorrectData() {
        //Given a list of tasks in the repository
        when(mTasksRepository.getTasks()).thenReturn(Observable.<List<Task>>empty());
        // And string resources
        withText(R.string.statistics_no_tasks, NO_TASKS);
=======
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
>>>>>>> StatisticsViewModel tests added

        //When subscribing to the statistics stream
        mViewModel.getStatistics().subscribe(mStatisticsTestSubscriber);

        //The correct pair is returned
<<<<<<< 9cd059efaef0fee63c4205745cd4f6c33cc5d24b
        String result = mStatisticsTestSubscriber.getOnNextEvents().get(0);
        assertThat(result, is(NO_TASKS));
=======
        Pair<Integer, Integer> result = mStatisticsTestSubscriber.getOnNextEvents().get(0);
        assertThat(result.first, is(0));
        assertThat(result.second, is(0));
>>>>>>> StatisticsViewModel tests added
    }

    @Test
    public void getProgressIndicator_emits_afterStatisticsAreRetrieved() {
        //Given a list of tasks in the repository
<<<<<<< 9cd059efaef0fee63c4205745cd4f6c33cc5d24b
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(mTasks));

        // And when subscribing to the progress indicator
        mViewModel.getProgressIndicator().subscribe(mProgressIndicatorTestSubscriber);
        //When subscribing to the statistics stream
        mViewModel.getStatistics().subscribe();

        // The intial value, false is emitted,
        // then values true and false were emitted
        mProgressIndicatorTestSubscriber.assertValues(false, true, false);
    }

    @Test
    public void getProgressIndicator_emits_afterStatisticsAreRetrieved_WithError() {
        //Given a list of tasks in the repository
        when(mTasksRepository.getTasks()).thenReturn(Observable.<List<Task>>error(new Exception()));
=======
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(TASKS));
>>>>>>> StatisticsViewModel tests added

        // And when subscribing to the progress indicator
        mViewModel.getProgressIndicator().subscribe(mProgressIndicatorTestSubscriber);
        //When subscribing to the statistics stream
        mViewModel.getStatistics().subscribe(mStatisticsTestSubscriber);

<<<<<<< 9cd059efaef0fee63c4205745cd4f6c33cc5d24b
        // The initial value, false is emitted,
        // then values true and false were emitted
        mProgressIndicatorTestSubscriber.assertValues(false, true, false);
    }

    private void withText(@StringRes int stringId, String returnedString) {
        when(mResourceProvider.getString(stringId)).thenReturn(returnedString);
=======
        // The values true and false were emitted
        mProgressIndicatorTestSubscriber.assertValues(true, false);
>>>>>>> StatisticsViewModel tests added
    }
}
