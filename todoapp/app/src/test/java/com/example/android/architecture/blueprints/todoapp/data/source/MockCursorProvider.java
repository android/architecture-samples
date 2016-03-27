package com.example.android.architecture.blueprints.todoapp.data.source;

import android.test.mock.MockCursor;

import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockCursorProvider {

    public static final String TITLE_TEST = "title";

    public static final String DESCRIPTION_TEST = "description";

    public static TaskMockCursor createActiveTaskCursor() {
        List<Map<Integer, Object>> entryList = new ArrayList<>();
        Map<Integer, Object> m = new HashMap<>();
        m.put(0, "1");
        m.put(1, TITLE_TEST);
        m.put(2, DESCRIPTION_TEST);
        m.put(3, 0);
        entryList.add(m);
        return new TaskMockCursor(entryList);
    }

    public static TaskMockCursor createCompletedTaskCursor() {
        List<Map<Integer, Object>> entryList = new ArrayList<>();
        Map<Integer, Object> m = new HashMap<>();
        m.put(0, "2");
        m.put(1, TITLE_TEST);
        m.put(2, DESCRIPTION_TEST);
        m.put(3, 1);
        entryList.add(m);
        return new TaskMockCursor(entryList);
    }

    public static TaskMockCursor createSampleTasksCursor() {
        List<Map<Integer, Object>> entryList = new ArrayList<>();
        Map<Integer, Object> task1 = new HashMap<>();
        task1.put(0, "1");
        task1.put(1, "Title1");
        task1.put(2, "Description1");
        task1.put(3, 1);
        entryList.add(task1);

        Map<Integer, Object> task2 = new HashMap<>();
        task1.put(0, "2");
        task1.put(1, "Title2");
        task1.put(2, "Description2");
        task1.put(3, 1);
        entryList.add(task2);

        Map<Integer, Object> task3 = new HashMap<>();
        task1.put(0, "3");
        task1.put(1, "Title3");
        task1.put(2, "Description3");
        task1.put(3, 1);
        entryList.add(task3);

        return new TaskMockCursor(entryList);
    }

    public static class TaskMockCursor extends MockCursor {
        Map<Integer, Object> entry;
        int cursorIndex;
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
            entry = entryList.get(cursorIndex);
            String value = (String) entry.get(columnIndex);
            return value;
        }

        private int getValueInt(int columnIndex) {
            entry = entryList.get(cursorIndex);
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
            cursorIndex++;
            return false;
        }
    }
}
