/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.google.samples.modularization.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.samples.modularization.feature.details.ui.DetailsRoute
import com.google.samples.modularization.feature.list.ui.ListRoute

@Composable
fun MainNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            ListRoute(
                onGoToItem = { id ->
                    navController.navigate("details/$id")
                }
            )
        }

        composable(
            "details/{id}",
            listOf(navArgument("id") { type = NavType.LongType })
        ) {
            DetailsRoute(
                onGoBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
