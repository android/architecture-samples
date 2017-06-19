package com.example.android.architecture.blueprints.todoapp.faq.datasourse.remote;

import com.example.android.architecture.blueprints.todoapp.faq.datasourse.FAQDataSource;
import com.example.android.architecture.blueprints.todoapp.faq.model.FAQModel;
import com.example.android.architecture.blueprints.todoapp.network.ApisUtils;

import java.util.List;

import rx.Observable;

/**
 * Created by Sa7r on 6/18/2017.
 */

public class FAQRemoteRepository implements FAQDataSource {
    public FAQRemoteRepository() {

    }

    @Override
    public Observable<List<FAQModel>> getListOfFAQs() {

        return getFaqs();
    }

    private Observable<List<FAQModel>> getFaqs() {

        // RxJava
        return ApisUtils.getFaqsAPIInstance().listFAQs();
    }
}
