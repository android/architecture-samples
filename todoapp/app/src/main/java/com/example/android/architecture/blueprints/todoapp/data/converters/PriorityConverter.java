package com.example.android.architecture.blueprints.todoapp.data.converters;

import androidx.room.TypeConverter;

import com.example.android.architecture.blueprints.todoapp.data.Priority;


public class PriorityConverter {

    @TypeConverter
    public static int convertProcurementTypeToInt(Priority priority) {
        return priority == null ? -1 : priority.ordinal();
    }

    @TypeConverter
    public static Priority convertIntToProcurementType(int enumOrdinal) {
        if (enumOrdinal == -1) {
            return null;
        } else {
            return Priority.values()[enumOrdinal];
        }
    }
}
