package com.example.android.architecture.blueprints.todoapp.faq.datasourse;

import com.example.android.architecture.blueprints.todoapp.faq.model.FAQModel;

import java.util.List;

import rx.Observable;

/**
 * Created by Sa7r on 6/15/2017.
 */

public interface FAQDataSource {
    Observable<List<FAQModel>> getListOfFAQs();
}
