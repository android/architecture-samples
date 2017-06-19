package com.example.android.architecture.blueprints.todoapp.faq.view;

import com.example.android.architecture.blueprints.todoapp.BaseView;
import com.example.android.architecture.blueprints.todoapp.faq.presenter.FAQPresenter;
import com.example.android.architecture.blueprints.todoapp.faq.model.FAQModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sa7r on 6/14/2017.
 */

public interface FAQViewContract extends BaseView<FAQPresenter> {
    void setProgressIndicator(boolean active);

    void showFAQ(List<FAQModel> faqModels);

    void showLoadingFAQError();

    boolean isActive();
}
