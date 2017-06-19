package com.example.android.architecture.blueprints.todoapp.faq;

import com.example.android.architecture.blueprints.todoapp.faq.datasourse.FAQRepository;
import com.example.android.architecture.blueprints.todoapp.faq.datasourse.fake.FAQFakeRepository;
import com.example.android.architecture.blueprints.todoapp.faq.datasourse.remote.FAQRemoteRepository;
import com.example.android.architecture.blueprints.todoapp.faq.domain.usecase.GetFaqUseCase;
import com.example.android.architecture.blueprints.todoapp.faq.model.FAQModel;
import com.example.android.architecture.blueprints.todoapp.faq.presenter.FAQPresenter;
import com.example.android.architecture.blueprints.todoapp.faq.view.FAQViewContract;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import rx.Observer;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

/**
 * Created by Sa7r on 6/18/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class FAQPresenterTest {
    private static List<FAQModel> faqModels;

    @Mock
    private FAQRepository faqRepository;
    @Mock
    private FAQFakeRepository fakeRepository;
    @Mock
    private FAQRemoteRepository remoteRepository;
    @Mock
    private FAQViewContract viewContract;


    private FAQPresenter faqPresenter;
    @Captor
    private ArgumentCaptor<Observer> mLoadObserverCaptor;

    @Before
    public void setupFAQPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        faqPresenter = givenTasksPresenter();

        // The presenter won't update the view unless it's active.
        Mockito.when(viewContract.isActive()).thenReturn(true);

        // We start the tasks to 3, with one active and two completed
        faqModels = Lists.newArrayList(new FAQModel("Title1", "Description1"),
                new FAQModel("Title2", "Description2"), new FAQModel("Title3", "Description3"));
    }

    private FAQPresenter givenTasksPresenter() {
        GetFaqUseCase getFaqUseCase = new GetFaqUseCase(faqRepository);

        return new FAQPresenter( viewContract,getFaqUseCase);
    }
//    @Test
//    public void loadAllFAQSFromRepositoryAndLoadIntoView() {
//
//       //fakeRepository.getListOfFAQs();
//
//        // Then progress indicator is shown
////        InOrder inOrder = inOrder(viewContract);
////        inOrder.verify(viewContract).setProgressIndicator(true);
//        // Then progress indicator is hidden and all tasks are shown in UI
////        inOrder.verify(viewContract).setProgressIndicator(false);
//        ArgumentCaptor<List> showTasksArgumentCaptor = ArgumentCaptor.forClass(List.class);
//        verify(viewContract).showFAQ(showTasksArgumentCaptor.capture());
//        assertTrue(showTasksArgumentCaptor.getValue().size() == 3);
//    }
}
