package com.app.autologinucab.presentation.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.app.autologinucab.presentation.ui.theme.AutoLoginUCABTheme
import org.junit.Rule
import org.junit.Test

class NetworksScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsTheCurrentListWithOneRemoveActionPerNetwork() {
        composeRule.setContent {
            AutoLoginUCABTheme {
                NetworksScreen(
                    state = NetworksScreenState(
                        ssids = listOf(
                            "Estudiantes.Ingenierias",
                            "Empleados.Ingenierias"
                        )
                    ),
                    actions = NetworksScreenActions()
                )
            }
        }

        composeRule
            .onNodeWithText("Estudiantes.Ingenierias")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule
            .onNodeWithText("Empleados.Ingenierias")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("Quitar").assertCountEquals(2)
    }

    @Test
    fun emptyListExplainsThatAutomationWillNotRun() {
        composeRule.setContent {
            AutoLoginUCABTheme {
                NetworksScreen(
                    state = NetworksScreenState(ssids = emptyList()),
                    actions = NetworksScreenActions()
                )
            }
        }

        composeRule
            .onNodeWithText("No hay redes permitidas. La automatización no se ejecutará hasta que agregues una.")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
