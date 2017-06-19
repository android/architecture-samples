package com.example.android.architecture.blueprints.todoapp.faq.domain.usecase;

import com.example.android.architecture.blueprints.todoapp.faq.datasourse.FAQRepository;
import com.example.android.architecture.blueprints.todoapp.faq.model.FAQModel;

import java.util.List;

import rx.Observable;


/**
 * Created by Sa7r on 6/14/2017.
 */

public class GetFaqUseCase {
    private final FAQRepository faqRepository;

    public GetFaqUseCase(FAQRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    public Observable<List<FAQModel>> executeUseCase() {
        Observable<List<FAQModel>> models = faqRepository.getListOfFAQs();
        return models;
    }

}
