package com.example.android.architecture.blueprints.todoapp.faq.datasourse.fake;

import com.example.android.architecture.blueprints.todoapp.faq.datasourse.FAQDataSource;
import com.example.android.architecture.blueprints.todoapp.faq.model.FAQModel;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by Sa7r on 6/18/2017.
 */

public class FAQFakeRepository implements FAQDataSource {
    public FAQFakeRepository(){

    }
    @Override
    public Observable<List<FAQModel>> getListOfFAQs() {
        ArrayList<FAQModel> models = new ArrayList<>();
        models.add(new FAQModel("question1", "answer for question1"));
        models.add(new FAQModel("question1", "answer for question1"));
        models.add(new FAQModel("question1", "answer for question1"));
        models.add(new FAQModel("question1", "answer for question1"));
        return Observable.from(models).toList();
    }
}
