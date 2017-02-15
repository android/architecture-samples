package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.app.Activity;

import com.example.android.architecture.blueprints.todoapp.util.providers.BaseNavigationProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Test class for {@link AddEditTaskNavigator}
 */
public class AddEditTaskNavigatorTest {

    @Mock
    private BaseNavigationProvider mNavigationProvider;

    private AddEditTaskNavigator mNavigator;

    @Before
    public void setUp() throws Exception {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mNavigator = new AddEditTaskNavigator(mNavigationProvider);
    }

    @Test
    public void onTaskSaved_finishesActivity() {
        // When the task is saved
        mNavigator.onTaskSaved();

        // The activity is finished with RESULT_OK
        Mockito.verify(mNavigationProvider).finishActivityWithResult(Activity.RESULT_OK);
    }
}