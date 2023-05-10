package com.google.samples.modularization.feature.list

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.google.samples.modularization.feature.list.ui.ListRoute
import com.google.samples.modularization.testing.HiltActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ListFeatureTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltActivity>()

    @Test
    fun `test item is displayed`() {
        composeTestRule.setContent {
            ListRoute(onGoToItem = { /* no-op */ })
        }
        composeTestRule.onNodeWithTag("item_1").assertExists()
    }
}