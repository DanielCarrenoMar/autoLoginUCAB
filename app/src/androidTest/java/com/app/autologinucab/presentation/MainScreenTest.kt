package com.app.autologinucab.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.app.autologinucab.domain.model.LoginAttempt
import com.app.autologinucab.domain.model.LoginResultType
import com.app.autologinucab.presentation.ui.theme.AutoLoginUCABTheme
import org.junit.Rule
import org.junit.Test

class MainScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsCredentialFieldsAndThePrimaryLoginAction() {
        composeRule.setContent {
            AutoLoginUCABTheme {
                MainScreen(state = MainScreenState(), actions = MainScreenActions())
            }
        }

        composeRule.onNodeWithText("Usuario").assertIsDisplayed()
        composeRule.onNodeWithText("Contraseña").assertIsDisplayed()
        composeRule.onNodeWithText("Redes permitidas").assertIsDisplayed()
        composeRule.onNodeWithText("Conectar ahora").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun explainsAMissingSsidPermissionWithAnAction() {
        composeRule.setContent {
            AutoLoginUCABTheme {
                MainScreen(
                    state = MainScreenState(permissionIssue = PermissionIssue.MISSING_PERMISSION),
                    actions = MainScreenActions()
                )
            }
        }

        composeRule
            .onNodeWithText("Permiso necesario para la automatización")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Conceder permiso").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun showsTheLastResultWithoutSensitiveData() {
        composeRule.setContent {
            AutoLoginUCABTheme {
                MainScreen(
                    state = MainScreenState(
                        lastAttempt = LoginAttempt(
                            type = LoginResultType.SUCCESS,
                            httpCode = 200,
                            timestamp = 0L
                        )
                    ),
                    actions = MainScreenActions()
                )
            }
        }

        composeRule
            .onNodeWithText("Conectado: internet validado.")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
