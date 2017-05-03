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

import java.util.ArrayList;
import java.util.List;

import rx.Completable;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link StatisticsViewModel}
 */
public class StatisticsViewModelTest {

    private static final String NO_TASKS = "no tasks";

    private static final String LOADING = "loading";
    private static final String LOADING_ERROR = "loading error";

    private List<Task> mTasks;

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private BaseResourceProvider mResourceProvider;

    private StatisticsViewModel mViewModel;

    private TestSubscriber<StatisticsUiModel> mTestSubscriber;

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
        mTestSubscriber = new TestSubscriber<>();
    }

    @Test
    public void getStatistics_emitsLoadingInitially() {
        //Given a list of tasks in the repository
        when(mTasksRepository.refreshTasks()).thenReturn(Completable.complete());
        when(mTasksRepository.getTasks()).thenReturn(Observable.never());

        withText(R.string.loading, LOADING);

        // When subscribing to the progress indicator
        mViewModel.getStatistics().subscribe(mTestSubscriber);

        // One value, that contains the string loading, is emitted
        mTestSubscriber.assertValueCount(1);
        assertEquals(mTestSubscriber.getOnNextEvents().get(0).getText(), LOADING);
    }

    @Test
    public void getStatistics_withTasks_returnsCorrectData() {
        //Given a list of tasks in the repository
        when(mTasksRepository.refreshTasks()).thenReturn(Completable.complete());
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(mTasks));

        //When subscribing to the statistics stream
        mViewModel.getStatistics().subscribe();

        //The correct pair is returned
        verify(mResourceProvider).getString(R.string.statistics_active_completed_tasks, 1, 2);
    }

    @Test
    public void getStatistics_withNoTasks_returnsCorrectData() {
        //Given a list of tasks in the repository
        when(mTasksRepository.refreshTasks()).thenReturn(Completable.complete());
        when(mTasksRepository.getTasks()).thenReturn(Observable.just(new ArrayList<>()));
        // And string resources
        withText(R.string.statistics_no_tasks, NO_TASKS);

        //When subscribing to the statistics stream
        mViewModel.getStatistics().subscribe(mTestSubscriber);

        //The correct text is returned
        String result = mTestSubscriber.getOnNextEvents().get(1).getText();
        assertEquals(result, NO_TASKS);
    }

    @Test
    public void getStatistics_emitsCorrectUiModel_afterStatisticsAreRetrieved_WithError() {
        //Given a list of tasks in the repository
        when(mTasksRepository.refreshTasks()).thenReturn(Completable.complete());
        when(mTasksRepository.getTasks()).thenReturn(Observable.error(new Exception()));
        // And a string to be returned for loading error
        withText(R.string.loading_tasks_error, LOADING_ERROR);

        //When subscribing to the statistics stream
        mViewModel.getStatistics().subscribe(mTestSubscriber);

        // The initial value, false is emitted,
        // then values true and false were emitted
        assertEquals(mTestSubscriber.getOnNextEvents().get(1).getText(), LOADING_ERROR);
    }

    private void withText(@StringRes int stringId, String returnedString) {
        when(mResourceProvider.getString(stringId)).thenReturn(returnedString);
    }
}
