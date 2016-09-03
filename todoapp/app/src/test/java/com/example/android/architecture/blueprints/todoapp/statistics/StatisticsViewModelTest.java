package com.example.android.architecture.blueprints.todoapp.statistics;

import android.support.annotation.StringRes;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.providers.BaseResourceProvider;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link StatisticsViewModel}
 */
public class StatisticsViewModelTest {

    private static final String NO_TASKS = "no tasks";
    private static final String ACTIVE_COMPLETED_TASKS = "Active tasks: %1$d \n Completed tasks: %2$d";

    private List<Task> mTasks;

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private BaseResourceProvider mResourceProvider;

    private StatisticsViewModel mViewModel;

    private TestSubscriber<Boolean> mProgressIndicatorTestSubscriber;

    private TestSubscriber<String> mStatisticsTestSubscriber;

    @Before
    public void setupStatisticsPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mViewModel = new StatisticsViewModel(mTasksRepository, mResourceProvider);

        // We subscribe the tasks to 3, with one active and two completed
        mTasks = Lists.newArrayList(
                new Task("Title1", "Description1"),
                new Task("Title2", "Description2", true),
                new Task("Title3", "Description3", true));
        mProgressIndicatorTestSubscriber = new TestSubscriber<>();
        mStatisticsTestSubscriber = new TestSubscriber<>();
    }

    @Test
    public void getProgressIndicator_emitsFalseInitially() {
        // When subscribing to the progress indicator
        mViewModel.getProgressIndicator().subscribe(mProgressIndicatorTestSubscriber);

        // One value: false, is emitted
        mProgressIndicatorTestSubscriber.assertValue(false);
    }

    @Test
    public void getStatistics_withTasks_returnsCorrectData() {
        //Given a list of tasks in the repository
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(mTasks));
        // And string resources
        withFormattedText(R.string.statistics_active_completed_tasks, ACTIVE_COMPLETED_TASKS);

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

        //When subscribing to the statistics stream
        mViewModel.getStatistics().subscribe(mStatisticsTestSubscriber);

        //The correct pair is returned
        String result = mStatisticsTestSubscriber.getOnNextEvents().get(0);
        assertThat(result, is(NO_TASKS));
    }

    @Test
    public void getProgressIndicator_emits_afterStatisticsAreRetrieved() {
        //Given a list of tasks in the repository
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

        // And when subscribing to the progress indicator
        mViewModel.getProgressIndicator().subscribe(mProgressIndicatorTestSubscriber);
        //When subscribing to the statistics stream
        mViewModel.getStatistics().subscribe(mStatisticsTestSubscriber);

        // The initial value, false is emitted,
        // then values true and false were emitted
        mProgressIndicatorTestSubscriber.assertValues(false, true, false);
    }

    private void withText(@StringRes int stringId, String returnedString) {
        when(mResourceProvider.getString(stringId)).thenReturn(returnedString);
    }

    private void withFormattedText(@StringRes int stringId, String returnedString) {
        when(mResourceProvider.getString(stringId, any(Object.class), any(Object.class))).thenReturn(returnedString);
    }

}
