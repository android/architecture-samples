package com.example.android.architecture.blueprints.todoapp.tasks;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.Observable;

import com.example.android.architecture.blueprints.todoapp.BR;
import com.example.android.architecture.blueprints.todoapp.R;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TasksViewModelTest {
    @Mock
    private Context mContext;

    @Mock
    private Resources mResources;

    @Mock
    private TasksPresenter mTasksPresenter;

    @Mock
    private Observable.OnPropertyChangedCallback mOnPropertyChangedCallback;

    private TasksViewModel mTasksViewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mContext.getResources()).thenReturn(mResources);
        mTasksViewModel = new TasksViewModel(mContext, mTasksPresenter);
    }

    @Test
    public void getCurrentFilteringLabel_allTasks() {
        when(mTasksPresenter.getFiltering()).thenReturn(TasksFilterType.ALL_TASKS);

        mTasksViewModel.getCurrentFilteringLabel();

        verify(mResources).getString(R.string.label_all);
    }

    @Test
    public void getNoTasksLabel_activeTasks() {
        when(mTasksPresenter.getFiltering()).thenReturn(TasksFilterType.ACTIVE_TASKS);

        mTasksViewModel.getNoTasksLabel();

        verify(mResources).getString(R.string.no_tasks_active);
    }

    @Test
    public void getNoTasksIconRes_completedTasks() {
        when(mTasksPresenter.getFiltering()).thenReturn(TasksFilterType.COMPLETED_TASKS);

        mTasksViewModel.getNoTaskIconRes();

        verify(mResources).getDrawable(R.drawable.ic_verified_user_24dp);
    }

    @Test
    public void getTasksAddViewVisible_allTasks() {
        when(mTasksPresenter.getFiltering()).thenReturn(TasksFilterType.ALL_TASKS);

        assertTrue(mTasksViewModel.getTasksAddViewVisible());
    }

    @Test
    public void getTasksAddViewVisible_activeTasks() {
        when(mTasksPresenter.getFiltering()).thenReturn(TasksFilterType.ACTIVE_TASKS);

        assertFalse(mTasksViewModel.getTasksAddViewVisible());
    }

    @Test
    public void isNotEmpty_initiallyEmpty() {
        assertFalse(mTasksViewModel.isNotEmpty());
    }

    @Test
    public void isNotEmpty_notEmpty() {
        mTasksViewModel.setTaskListSize(1);

        assertTrue(mTasksViewModel.isNotEmpty());
    }

    @Test
    public void setTaskListSize_notifiesProperties() {
        mTasksViewModel.addOnPropertyChangedCallback(mOnPropertyChangedCallback);

        mTasksViewModel.setTaskListSize(0);

        verify(mOnPropertyChangedCallback).onPropertyChanged(mTasksViewModel, BR.noTaskIconRes);
        verify(mOnPropertyChangedCallback).onPropertyChanged(mTasksViewModel, BR.noTasksLabel);
        verify(mOnPropertyChangedCallback).onPropertyChanged(mTasksViewModel, BR.currentFilteringLabel);
        verify(mOnPropertyChangedCallback).onPropertyChanged(mTasksViewModel, BR.notEmpty);
        verify(mOnPropertyChangedCallback).onPropertyChanged(mTasksViewModel, BR.tasksAddViewVisible);
    }
}