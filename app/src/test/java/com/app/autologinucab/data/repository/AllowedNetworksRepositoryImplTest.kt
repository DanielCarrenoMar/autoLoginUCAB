package com.app.autologinucab.data.repository

import com.app.autologinucab.domain.model.SsidRules
import com.app.autologinucab.domain.repository.SsidSettingsStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AllowedNetworksRepositoryImplTest {

    private val store = InMemorySsidSettingsStore()
    private val repository = AllowedNetworksRepositoryImpl(store)

    @Test
    fun firstUseSeedsExactlyTheFourDefaultNetworks() {
        val ssids = repository.allowedSsids()

        assertEquals(
            listOf(
                "Estudiantes.Ingenierias",
                "Empleados.Ingenierias",
                "Empleados.Mod_AR_IV",
                "Estudiantes.Mod_AR_IV"
            ),
            ssids
        )
        assertEquals(ssids, store.saved)
    }

    @Test
    fun secondReadUsesThePersistedListWithoutReseeding() {
        repository.allowedSsids()
        store.saved = listOf("Solo.Esta")

        assertEquals(listOf("Solo.Esta"), repository.allowedSsids())
        assertEquals(listOf("Solo.Esta"), store.saved)
    }

    @Test
    fun intentionallyEmptyListIsPreserved() {
        store.saved = emptyList()

        assertEquals(emptyList<String>(), repository.allowedSsids())
        assertEquals(emptyList<String>(), store.saved)
    }

    @Test
    fun addStoresNormalizedSsidAndRejectsDuplicates() {
        assertTrue(repository.add("  \"Nueva.Red\"  "))
        assertEquals("Nueva.Red", repository.allowedSsids().last())

        assertFalse(repository.add("Nueva.Red"))
        assertEquals(1, repository.allowedSsids().count { it == "Nueva.Red" })
    }

    @Test
    fun addRejectsBlankInput() {
        assertFalse(repository.add("   "))
        assertFalse(repository.add("\"\""))
        assertNull(store.saved)
    }

    @Test
    fun removePersistsTheRemainingNetworks() {
        repository.remove("Empleados.Mod_AR_IV")

        assertEquals(
            listOf(
                "Estudiantes.Ingenierias",
                "Empleados.Ingenierias",
                "Estudiantes.Mod_AR_IV"
            ),
            repository.allowedSsids()
        )
    }

    @Test
    fun removeCanProduceAnIntentionallyEmptyList() {
        SsidRules.DEFAULT_SSIDS.forEach(repository::remove)

        assertEquals(emptyList<String>(), repository.allowedSsids())
        assertEquals(emptyList<String>(), store.saved)
    }

    @Test
    fun isAllowedMatchesExactlyAfterRemovingSurroundingQuotes() {
        assertTrue(repository.isAllowed("\"Estudiantes.Ingenierias\""))
        assertTrue(repository.isAllowed("Estudiantes.Ingenierias"))
        assertFalse(repository.isAllowed("estudiantes.ingenierias"))
        assertFalse(repository.isAllowed("Estudiantes.Ingenierias.Otra"))
        assertFalse(repository.isAllowed(null))
        assertFalse(repository.isAllowed(SsidRules.UNKNOWN_SSID))
    }

    private class InMemorySsidSettingsStore : SsidSettingsStore {
        var saved: List<String>? = null

        override fun load(): List<String>? = saved

        override fun save(ssids: List<String>) {
            saved = ssids
        }
    }
}
