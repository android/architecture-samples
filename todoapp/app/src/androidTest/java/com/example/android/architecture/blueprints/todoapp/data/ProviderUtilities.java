package com.example.android.architecture.blueprints.todoapp.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ProviderUtilities extends AndroidTestCase{

    static ContentValues createTasksValues() {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID, UUID.randomUUID().toString());
        weatherValues.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE, "Title1");
        weatherValues.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION, "Description1");
        weatherValues.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, 0);

        weatherValues.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID, UUID.randomUUID().toString());
        weatherValues.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE, "Title2");
        weatherValues.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION, "Description2");
        weatherValues.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, 0);

        return weatherValues;
    }

    static void assertCursorEmpty(Cursor valueCursor) {
        assertFalse("Non Empty cursor returned", valueCursor.moveToFirst());
    }

    static void assertCursorHasData(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                                 "' did not match the expected value '" +
                                 expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

}
