package com.example.android.architecture.blueprints.todoapp.faq.presenter;

import com.example.android.architecture.blueprints.todoapp.BasePresenter;
import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.UseCaseHandler;
import com.example.android.architecture.blueprints.todoapp.faq.model.FAQModel;
import com.example.android.architecture.blueprints.todoapp.faq.domain.usecase.GetFaqUseCase;
import com.example.android.architecture.blueprints.todoapp.faq.view.FAQViewContract;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Sa7r on 6/14/2017.
 */

public class FAQPresenter implements BasePresenter {
    private final FAQViewContract view;
    private final GetFaqUseCase getFaqUseCase;

    public FAQPresenter(FAQViewContract view, GetFaqUseCase getFaqUseCase) {
        this.view = view;
        this.getFaqUseCase = getFaqUseCase;
    }

    @Override
    public void start() {
        loadFAQ();
    }

    private void loadFAQ() {
        view.setProgressIndicator(true);

        Observable<List<FAQModel>> faqObservable = getFaqUseCase.executeUseCase();
        faqObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<FAQModel>>() {
                    @Override
                    public void onCompleted() {
                        view.setProgressIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        view.showLoadingFAQError();
                    }

                    @Override
                    public void onNext(List<FAQModel> faqModels) {
                        view.showFAQ(faqModels);
                    }
                });
    }
}
