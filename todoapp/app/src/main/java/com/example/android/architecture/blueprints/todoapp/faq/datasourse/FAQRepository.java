package com.example.android.architecture.blueprints.todoapp.faq.datasourse;

import com.example.android.architecture.blueprints.todoapp.faq.datasourse.fake.FAQFakeRepository;
import com.example.android.architecture.blueprints.todoapp.faq.datasourse.remote.FAQRemoteRepository;
import com.example.android.architecture.blueprints.todoapp.faq.model.FAQModel;

import java.util.List;

import rx.Observable;

/**
 * Created by Sa7r on 6/14/2017.
 */

public class FAQRepository {
    private static FAQRepository INSTANCE = null;
    private final FAQFakeRepository faqFakeRepository;
    private  FAQRemoteRepository faqRemoteRepository;

    public FAQRepository(FAQRemoteRepository faqRemoteRepository, FAQFakeRepository faqFakeRepository) {
        this.faqRemoteRepository=faqRemoteRepository;
        this.faqFakeRepository=faqFakeRepository;

    }
    public static FAQRepository getInstance(FAQRemoteRepository faqRemoteRepository, FAQFakeRepository faqFakeRepository) {
        if (INSTANCE == null) {
            INSTANCE = new FAQRepository(faqRemoteRepository,faqFakeRepository);
        }
        return INSTANCE;
    }


    public Observable<List<FAQModel>> getListOfFAQs() {
        return faqRemoteRepository.getListOfFAQs();
    }
}
