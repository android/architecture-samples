package com.example.android.architecture.blueprints.todoapp.faq;

import android.support.test.runner.AndroidJUnit4;

import com.example.android.architecture.blueprints.todoapp.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Sa7r on 6/18/2017.
 */
@RunWith(AndroidJUnit4.class)

public class FAQScreenTest {
    @Test
    public void viewAllFAQs() {
        onView(withId(R.id.faq_navigation_menu_item)).perform(click());
    }
}
