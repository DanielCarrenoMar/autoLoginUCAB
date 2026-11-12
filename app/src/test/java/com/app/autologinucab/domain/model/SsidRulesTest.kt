package com.app.autologinucab.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SsidRulesTest {

    @Test
    fun stripsOnlyAndroidSurroundingQuotes() {
        assertEquals("Estudiantes.Ingenierias", SsidRules.normalize("\"Estudiantes.Ingenierias\""))
        assertEquals("\"mitad", SsidRules.normalize("\"mitad"))
        assertEquals("mitad\"", SsidRules.normalize("mitad\""))
        assertEquals("Con \"Comillas\" Internas", SsidRules.normalize("\"Con \"Comillas\" Internas\""))
    }

    @Test
    fun trimsSurroundingWhitespace() {
        assertEquals("Red", SsidRules.normalize("  Red  "))
        assertEquals("Red", SsidRules.normalize(" \"Red\" "))
    }

    @Test
    fun rejectsBlankAndUnknownSsids() {
        assertNull(SsidRules.normalize(null))
        assertNull(SsidRules.normalize(""))
        assertNull(SsidRules.normalize("   "))
        assertNull(SsidRules.normalize("\"\""))
        assertNull(SsidRules.normalize(SsidRules.UNKNOWN_SSID))
    }
}
