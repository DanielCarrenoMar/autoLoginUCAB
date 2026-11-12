package com.app.autologinucab.domain

import com.app.autologinucab.domain.usecase.RepeatedConnectionGuard
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RepeatedConnectionGuardTest {

    private val guard = RepeatedConnectionGuard(retryWindowMillis = 60_000L)

    @Test
    fun firstAttemptForAConnectionIsAllowed() {
        assertTrue(guard.shouldAttempt("Estudiantes.Ingenierias", 1_000L))
    }

    @Test
    fun repeatedCallbackForTheSameConnectionInsideTheWindowIsIgnored() {
        guard.shouldAttempt("Estudiantes.Ingenierias", 1_000L)

        assertFalse(guard.shouldAttempt("Estudiantes.Ingenierias", 2_000L))
    }

    @Test
    fun sameConnectionIsRetriedAfterTheWindow() {
        guard.shouldAttempt("Estudiantes.Ingenierias", 1_000L)

        assertTrue(guard.shouldAttempt("Estudiantes.Ingenierias", 61_000L))
    }

    @Test
    fun aDifferentSsidIsAlwaysAllowed() {
        guard.shouldAttempt("Estudiantes.Ingenierias", 1_000L)

        assertTrue(guard.shouldAttempt("Empleados.Ingenierias", 2_000L))
    }

    @Test
    fun resetAllowsTheSameConnectionAgain() {
        guard.shouldAttempt("Estudiantes.Ingenierias", 1_000L)
        guard.reset()

        assertTrue(guard.shouldAttempt("Estudiantes.Ingenierias", 2_000L))
    }
}
