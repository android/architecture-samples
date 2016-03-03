package com.example.android.architecture.blueprints.todoapp.addedittask;

import dagger.Module;
import dagger.Provides;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link AddEditTaskPresenter}.
 */
@Module
public class AddEditTaskPresenterModule {

    private final AddEditTaskContract.View mView;

    public AddEditTaskPresenterModule(AddEditTaskContract.View view) {
        mView = view;
    }

    @Provides
    AddEditTaskContract.View provideAddEditTaskContractView() {
        return mView;
    }
}
