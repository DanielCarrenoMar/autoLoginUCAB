package com.app.autologinucab.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PollingInternetValidatorTest {

    private var now = 0L

    @Test
    fun returnsTrueImmediatelyWhenInternetIsAlreadyValidated() {
        var probes = 0
        val validator = validatorWith { probes++; true }

        assertTrue(validator.awaitValidated(5_000))

        assertEquals(1, probes)
        assertEquals(0L, now)
    }

    @Test
    fun waitsUntilValidationAppearsWithinTheTimeout() {
        var probes = 0
        val validator = validatorWith { probes++; probes >= 3 }

        assertTrue(validator.awaitValidated(5_000))

        assertEquals(3, probes)
        assertEquals(1_000L, now)
    }

    @Test
    fun returnsFalseAtTheTimeoutBoundary() {
        val validator = validatorWith { false }

        assertFalse(validator.awaitValidated(1_000))

        assertEquals(1_000L, now)
    }

    @Test
    fun zeroTimeoutChecksOnceWithoutSleeping() {
        var probes = 0
        val validator = validatorWith { probes++; false }

        assertFalse(validator.awaitValidated(0))

        assertEquals(1, probes)
        assertEquals(0L, now)
    }

    private fun validatorWith(probe: () -> Boolean) = PollingInternetValidator(
        isInternetAvailable = probe,
        nowMillis = { now },
        sleep = { millis -> now += millis }
    )
}
