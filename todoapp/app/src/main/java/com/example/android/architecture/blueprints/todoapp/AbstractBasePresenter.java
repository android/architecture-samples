package com.example.android.architecture.blueprints.todoapp;

/**
 * This is the parent of all presenters in this project; it also provides the most
 * fundamental implementation of {@link BasePresenter}. The main purpose of this class
 * is it to prevent leaking memory whenever we execute use cases.
 *
 * A memory leak will occur if we execute a long-running use case, that references the
 * presenter's view in the callback of the use case (anonymous inner-class), and the
 * implementation of the view (Activity/Fragment/View)'s lifecycle has ended before the
 * completion of the use case.
 *
 * We could experiment with various usages of {@link java.lang.ref.WeakReference}
 * to hold references to the views, but all the ways I've tried worked to no avail.
 *
 * The best way that I have found to prevent the above mentioned memory leak from by
 * manually removing the reference to the presenter's view in the appropriate Android
 * framework lifecycle callback. This is why I have added the {@link #attach(BaseView)}
 * and {@link #detach()} methods to {@link BasePresenter}. We'll attach the view to the
 * presenter whenever appropriate and then detach the view whenever appropriate as well
 * for each presenter we'll create.
 *
 * Please note that doing the above solution (attach/detach the presenter's view) implicates
 * a {@link NullPointerException} whenever the use case's callback attempts to call a method
 * on the presenter's view that has been detached. As a solution to this problem there's two
 * ways I've found to fix this:
 *
 * (1) Wrapping the use case's callback in a nested inner-class that checks if the presenter's
 * view is null before invoking the wrapped callback's methods, and
 *
 * (2) Setting up a visitor pattern in the use case scheduler and creating an implementation
 * of it in this presenter to visit if the view is null before invoking the use case callback's
 * methods.
 *
 * This example uses the first solutions.
 *
 *
 * @author Tyler Suehr
 * @version 1.0
 */
public abstract class AbstractBasePresenter<IView extends BaseView> implements BasePresenter<IView> {
    /* Stores reference to the presenter's use case handler */
    private final UseCaseHandler mUseCaseHandler;
    /* Stores reference to the presenter's view */
    protected IView mView;


    public AbstractBasePresenter(final UseCaseHandler handler) {
        this.mUseCaseHandler = handler;
    }

    public AbstractBasePresenter(final IView view, final UseCaseHandler handler) {
        this.mView = view;
        this.mUseCaseHandler = handler;
    }

    @Override
    public void attach(IView view) {
        this.mView = view;
    }

    @Override
    public void detach() {
        this.mView = null;
    }

    /**
     * This will schedule the execution of a use case with {@link #mUseCaseHandler}.
     *
     * The main purpose of this method is to wrap the given callback in another implementation
     * that will ensure the presenter's view isn't null when invoking the wrapped callback's
     * methods.
     *
     * @param useCase {@link UseCase}
     * @param request Request data
     * @param callback {@link UseCase.UseCaseCallback}
     * @param <T> Request data type
     * @param <V> Response data type
     */
    protected <T extends UseCase.RequestValues,V extends UseCase.ResponseValue> void
    schedule(final UseCase<T,V> useCase, final T request, final UseCase.UseCaseCallback<V> callback) {
        this.mUseCaseHandler.execute(useCase, request, new NullCheckWrapper<>(callback));
    }


    /**
     * Nested inner-class that will wrap a given use case callback and will ensure that the
     * presenter's view is not null before invoking the wrapped callback's methods.
     */
    private final class NullCheckWrapper<V extends UseCase.ResponseValue> implements UseCase.UseCaseCallback<V> {
        private final UseCase.UseCaseCallback<V> mWrap;

        NullCheckWrapper(final UseCase.UseCaseCallback<V> wrap) {
            this.mWrap = wrap;
        }

        @Override
        public void onSuccess(V response) {
            if (mView != null) { this.mWrap.onSuccess(response); }
        }

        @Override
        public void onError() {
            if (mView != null) { this.mWrap.onError(); }
        }
    }
}