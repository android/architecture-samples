/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.custom.action;

import android.content.res.Resources.NotFoundException;
import android.support.design.widget.NavigationView;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.espresso.util.HumanReadables;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static org.hamcrest.Matchers.allOf;

/**
 * View actions for interacting with {@link NavigationView}
 */
public final class NavigationViewActions {

    private NavigationViewActions() {
        // no Instance
    }

    /**
     * Returns a {@link ViewAction} that navigates to a menu item in {@link NavigationView} using a
     * menu item resource id.
     * <p>
     * <p>
     * View constraints:
     * <ul>
     * <li>View must be a child of a {@link DrawerLayout}
     * <li>View must be of type {@link NavigationView}
     * <li>View must be visible on screen
     * <li>View must be displayed on screen
     * <ul>
     *
     * @param menuItemId the resource id of the menu item
     * @return a {@link ViewAction} that navigates on a menu item
     */
    public static ViewAction navigateTo(final int menuItemId) {

        return new ViewAction() {

            @Override
            public void perform(UiController uiController, View view) {
                NavigationView navigationView = (NavigationView) view;
                Menu menu = navigationView.getMenu();
                if (null == menu.findItem(menuItemId)) {
                    throw new PerformException.Builder()
                            .withActionDescription(this.getDescription())
                            .withViewDescription(HumanReadables.describe(view))
                            .withCause(new RuntimeException(getErrorMessage(menu, view)))
                            .build();
                }
                menu.performIdentifierAction(menuItemId, 0);
                uiController.loopMainThreadUntilIdle();
            }

            private String getErrorMessage(Menu menu, View view) {
                String NEW_LINE = System.getProperty("line.separator");
                StringBuilder errorMessage = new StringBuilder("Menu item was not found, "
                        + "available menu items:")
                        .append(NEW_LINE);
                for (int position = 0; position < menu.size(); position++) {
                    errorMessage.append("[MenuItem] position=")
                            .append(position);
                    MenuItem menuItem = menu.getItem(position);
                    if (menuItem != null) {
                        CharSequence itemTitle = menuItem.getTitle();
                        if (itemTitle != null) {
                            errorMessage.append(", title=")
                                    .append(itemTitle);
                        }
                        if (view.getResources() != null) {
                            int itemId = menuItem.getItemId();
                            try {
                                errorMessage.append(", id=");
                                String menuItemResourceName = view.getResources()
                                        .getResourceName(itemId);
                                errorMessage.append(menuItemResourceName);
                            } catch (NotFoundException nfe) {
                                errorMessage.append("not found");
                            }
                        }
                        errorMessage.append(NEW_LINE);
                    }
                }
                return errorMessage.toString();
            }

            @Override
            public String getDescription() {
                return "click on menu item with id";
            }

            @Override
            public Matcher<View> getConstraints() {
                return allOf(isAssignableFrom(NavigationView.class),
                        withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                        isDisplayingAtLeast(90)
                );
            }
        };

    }
}
