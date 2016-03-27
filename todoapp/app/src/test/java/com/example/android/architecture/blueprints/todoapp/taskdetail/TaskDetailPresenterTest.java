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

package com.example.android.architecture.blueprints.todoapp.taskdetail;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.test.mock.MockCursor;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TaskCursorLoader;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

/**
 * Unit tests for the implementation of {@link TaskDetailPresenter}
 */
public class TaskDetailPresenterTest {

    public static final String TITLE_TEST = "title";

    public static final String DESCRIPTION_TEST = "description";

    public static final String INVALID_TASK_ID = "";

    public static final Task ACTIVE_TASK = new Task(TITLE_TEST, DESCRIPTION_TEST);

    public static final Task COMPLETED_TASK = new Task(TITLE_TEST, DESCRIPTION_TEST, true);

    @Mock
    private TaskDetailContract.View mTaskDetailFragment;

    @Mock
    private TaskCursorLoader mTaskLoader;

    @Mock
    private LoaderManager mLoaderManager;

    @Mock
    private TasksRepository mTasksRepository;

    private TaskMockCursor mActiveTaskCursor;
    private TaskMockCursor mCompletedTaskCursor;

    private TaskDetailPresenter mTaskDetailPresenter;

    @Before
    public void setup() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        initActiveTaskCursor();
        initCompletedTaskCursor();
    }

    private void initActiveTaskCursor() {
        List<Map<Integer, Object>> entryList = new ArrayList<>();
        Map<Integer, Object> m = new HashMap<>();
        m.put(0, "1");
        m.put(1, TITLE_TEST);
        m.put(2, DESCRIPTION_TEST);
        m.put(3, 0);
        entryList.add(m);
        mActiveTaskCursor = new TaskMockCursor(entryList);
    }

    private void initCompletedTaskCursor() {
        List<Map<Integer, Object>> entryList = new ArrayList<>();
        Map<Integer, Object> m = new HashMap<>();
        m.put(0, "2");
        m.put(1, TITLE_TEST);
        m.put(2, DESCRIPTION_TEST);
        m.put(3, 1);
        entryList.add(m);
        mCompletedTaskCursor = new TaskMockCursor(entryList);
    }

    @Test
    public void getActiveTaskFromRepositoryAndLoadIntoView() {
        // Get a reference to the class under test
        mTaskDetailPresenter = new TaskDetailPresenter(
                ACTIVE_TASK.getId(), mTasksRepository, mTaskDetailFragment, mTaskLoader,
                mLoaderManager
        );

        // When tasks presenter is asked to open an ACTIVE_TASK
        mTaskDetailPresenter.onLoadFinished(mock(Loader.class), mActiveTaskCursor);

        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        verify(mTaskDetailFragment).setLoadingIndicator(false);
        verify(mTaskDetailFragment).showTitle(TITLE_TEST);
        verify(mTaskDetailFragment).showDescription(DESCRIPTION_TEST);
    }

    @Test
    public void getCompletedTaskFromRepositoryAndLoadIntoView() {
        // When tasks presenter is asked to open a completed task
        mTaskDetailPresenter = new TaskDetailPresenter(COMPLETED_TASK.getId(), mTasksRepository,
                                                       mTaskDetailFragment, mTaskLoader, mLoaderManager
        );
        mTaskDetailPresenter.onLoadFinished(mock(Loader.class), mCompletedTaskCursor);

        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        verify(mTaskDetailFragment).setLoadingIndicator(false);
        verify(mTaskDetailFragment).showTitle(TITLE_TEST);
        verify(mTaskDetailFragment).showDescription(DESCRIPTION_TEST);
        verify(mTaskDetailFragment).showCompletionStatus(true);
    }

    @Test
    public void getUnknownTaskFromRepositoryAndLoadIntoView() {
        // When loading of an ACTIVE_TASK is requested with an invalid task
        mTaskDetailPresenter = new TaskDetailPresenter(INVALID_TASK_ID, mTasksRepository,
                                                       mTaskDetailFragment, mTaskLoader, mLoaderManager
        );
        mTaskDetailPresenter.onLoadFinished(mock(Loader.class), null);

        // Then progress indicator is hidden and missing ACTIVE_TASK UI is shown
        verify(mTaskDetailFragment).showMissingTask();
    }

    @Test
    public void deleteTask() {
        // When the deletion of an ACTIVE_TASK is requested
        mTaskDetailPresenter = new TaskDetailPresenter(ACTIVE_TASK.getId(), mTasksRepository,
                                                       mTaskDetailFragment, mTaskLoader, mLoaderManager
        );
        mTaskDetailPresenter.deleteTask();

        // Then the repository and the view are notified
        verify(mTasksRepository).deleteTask(ACTIVE_TASK.getId());
        verify(mTaskDetailFragment).showTaskDeleted();
    }

    @Test
    public void completeTask() {
        // When the presenter is asked to complete the ACTIVE_TASK
        mTaskDetailPresenter = new TaskDetailPresenter(ACTIVE_TASK.getId(), mTasksRepository,
                                                       mTaskDetailFragment, mTaskLoader, mLoaderManager
        );
        mTaskDetailPresenter.completeTask();

        // Then a request is sent to the repository and the UI is updated
        verify(mTasksRepository).completeTask(ACTIVE_TASK.getId());
        verify(mTaskDetailFragment).showTaskMarkedComplete();
    }

    @Test
    public void activateTask() {
        // When the presenter is asked to activate the ACTIVE_TASK
        mTaskDetailPresenter = new TaskDetailPresenter(ACTIVE_TASK.getId(), mTasksRepository,
                                                       mTaskDetailFragment, mTaskLoader, mLoaderManager
        );
        mTaskDetailPresenter.activateTask();

        // Then a request is sent to the repository and the UI is updated
        verify(mTasksRepository).activateTask(ACTIVE_TASK.getId());
        verify(mTaskDetailFragment).showTaskMarkedActive();
    }

    @Test
    public void activeTaskIsShownWhenEditing() {
        // When the edit of an ACTIVE_TASK is requested
        mTaskDetailPresenter = new TaskDetailPresenter(ACTIVE_TASK.getId(), mTasksRepository,
                                                       mTaskDetailFragment, mTaskLoader, mLoaderManager
        );
        mTaskDetailPresenter.editTask();

        // Then the view is notified
        verify(mTaskDetailFragment).showEditTask(ACTIVE_TASK.getId());
    }

    @Test
    public void invalidTaskIsNotShownWhenEditing() {
        // When the edit of an invalid task id is requested
        mTaskDetailPresenter = new TaskDetailPresenter(INVALID_TASK_ID, mTasksRepository,
                                                       mTaskDetailFragment, mTaskLoader, mLoaderManager
        );
        mTaskDetailPresenter.editTask();

        // Then the edit mode is never started
        verify(mTaskDetailFragment, never()).showEditTask(INVALID_TASK_ID);
        // instead, the error is shown.
        verify(mTaskDetailFragment).showMissingTask();
    }

    public class TaskMockCursor extends MockCursor {

        Map<Integer, Object> entry;
        List<Map<Integer, Object>> entryList;
        Map<String, Integer> columnIndexes;

        {
            columnIndexes = new HashMap<>();
            columnIndexes.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID, 0);
            columnIndexes.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE, 1);
            columnIndexes.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION, 2);
            columnIndexes.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, 3);
        }

        public TaskMockCursor(List<Map<Integer, Object>> entryList) {
            this.entryList = entryList;
        }

        @Override
        public String getString(int columnIndex) {
            return getValueString(columnIndex);
        }

        @Override
        public float getFloat(int columnIndex) {
            return Float.parseFloat(getValueString(columnIndex));
        }

        @Override
        public int getInt(int columnIndex) {
            return getValueInt(columnIndex);
        }

        private String getValueString(int columnIndex) {
            entry = entryList.get(0);
            String value = (String) entry.get(columnIndex);
            return value;
        }

        private int getValueInt(int columnIndex) {
            entry = entryList.get(0);
            int value = (int) entry.get(columnIndex);
            return value;
        }

        @Override
        public int getColumnIndex(String columnName) {
            return Integer.valueOf(columnIndexes.get(columnName));
        }

        @Override
        public int getColumnIndexOrThrow(String columnName) {
            return Integer.valueOf(columnIndexes.get(columnName));
        }

        @Override
        public boolean moveToFirst() {
            return true;
        }

        @Override
        public boolean moveToNext() {
            return false;
        }
    }
}


