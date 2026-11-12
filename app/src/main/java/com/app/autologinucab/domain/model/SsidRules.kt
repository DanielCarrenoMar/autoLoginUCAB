package com.app.autologinucab.domain.model

/**
 * Pure SSID normalization shared by the stored allow-list and the Android adapters.
 *
 * Android reports surrounding quotes around the SSID on several versions, so only those
 * quotes are removed before comparing. No other transformation is applied beyond trimming
 * surrounding whitespace and rejecting Android's placeholder for an unknown network.
 */
object SsidRules {

    val DEFAULT_SSIDS: List<String> = listOf(
        "Estudiantes.Ingenierias",
        "Empleados.Ingenierias",
        "Empleados.Mod_AR_IV",
        "Estudiantes.Mod_AR_IV",
        "Estudiantes.Bilioteca",
        "Empleados.Bilioteca",
    )

    const val UNKNOWN_SSID = "<unknown ssid>"

    fun normalize(raw: String?): String? {
        val trimmed = raw?.trim().orEmpty()
        if (trimmed.isEmpty() || trimmed == UNKNOWN_SSID) return null

        val withoutQuotes = if (
            trimmed.length >= 2 &&
            trimmed.startsWith('"') &&
            trimmed.endsWith('"')
        ) {
            trimmed.substring(1, trimmed.length - 1)
        } else {
            trimmed
        }

        return withoutQuotes.trim().ifEmpty { null }
    }
}
