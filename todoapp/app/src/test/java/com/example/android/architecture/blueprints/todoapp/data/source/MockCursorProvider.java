package com.example.android.architecture.blueprints.todoapp.data.source;

import android.test.mock.MockCursor;

import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MockCursorProvider {

    public static final String TITLE_TEST = "Title";
    public static final String DESCRIPTION_TEST = "Description";

    private static Map<Integer, Object> createActiveTaskCursorEntry(){
        Map<Integer, Object> entry = new HashMap<>();
        entry.put(0, 0);
        entry.put(1, UUID.randomUUID().toString());
        entry.put(2, TITLE_TEST);
        entry.put(3, DESCRIPTION_TEST);
        entry.put(4, 0);
        return entry;
    }

    private static Map<Integer, Object> createCompletedTaskCursorEntry() {
        Map<Integer, Object> entry = new HashMap<>();
        entry.put(0, 0);
        entry.put(1, UUID.randomUUID().toString());
        entry.put(2, TITLE_TEST);
        entry.put(3, DESCRIPTION_TEST);
        entry.put(4, 1);
        return entry;
    }

    public static TaskMockCursor createActiveTaskCursor() {
        List<Map<Integer, Object>> entryList = new ArrayList<>();
        entryList.add(createActiveTaskCursorEntry());
        return new TaskMockCursor(entryList);
    }

    public static TaskMockCursor createCompletedTaskCursor() {
        List<Map<Integer, Object>> entryList = new ArrayList<>();
        entryList.add(createCompletedTaskCursorEntry());
        return new TaskMockCursor(entryList);
    }

    public static TaskMockCursor createCompletedTasksCursor() {
        List<Map<Integer, Object>> entryList = new ArrayList<>();
        entryList.add(createCompletedTaskCursorEntry());
        entryList.add(createCompletedTaskCursorEntry());
        entryList.add(createCompletedTaskCursorEntry());
        return new TaskMockCursor(entryList);
    }

    public static TaskMockCursor createActiveTasksCursor() {
        List<Map<Integer, Object>> entryList = new ArrayList<>();
        entryList.add(createActiveTaskCursorEntry());
        entryList.add(createActiveTaskCursorEntry());
        return new TaskMockCursor(entryList);
    }

    public static TaskMockCursor createAllTasksCursor() {
        List<Map<Integer, Object>> entryList = new ArrayList<>();
        entryList.add(createCompletedTaskCursorEntry());
        entryList.add(createCompletedTaskCursorEntry());
        entryList.add(createCompletedTaskCursorEntry());
        entryList.add(createActiveTaskCursorEntry());
        entryList.add(createActiveTaskCursorEntry());
        return new TaskMockCursor(entryList);
    }

    public static class TaskMockCursor extends MockCursor {
        Map<Integer, Object> entry;
        int cursorIndex;
        List<Map<Integer, Object>> entryList;
        Map<String, Integer> columnIndexes;

        {
            columnIndexes = new HashMap<>();
            columnIndexes.put(TasksPersistenceContract.TaskEntry._ID, 0);
            columnIndexes.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID, 1);
            columnIndexes.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE, 2);
            columnIndexes.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION, 3);
            columnIndexes.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, 4);
        }

        public TaskMockCursor(List<Map<Integer, Object>> entryList) {
            this.entryList = entryList;
        }

        @Override
        public int getCount() {
            return entryList.size();
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
