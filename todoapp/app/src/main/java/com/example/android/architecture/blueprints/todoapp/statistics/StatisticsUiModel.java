package com.example.android.architecture.blueprints.todoapp.statistics;

/**
 * Model for the Statistics View.
 */
class StatisticsUiModel {

    private String mText;

    StatisticsUiModel(String text) {
        mText = text;
    }

    /**
     * @return text that should be displayed in the Statistics screen.
     */
    public String getText() {
        return mText;
    }
}
