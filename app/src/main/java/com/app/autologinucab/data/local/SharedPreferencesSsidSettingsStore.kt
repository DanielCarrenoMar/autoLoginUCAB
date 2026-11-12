package com.app.autologinucab.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.app.autologinucab.domain.repository.SsidSettingsStore

class SharedPreferencesSsidSettingsStore(context: Context) : SsidSettingsStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): List<String>? {
        if (!prefs.getBoolean(KEY_SEEDED, false)) return null

        val raw = prefs.getString(KEY_SSIDS, "").orEmpty()
        if (raw.isEmpty()) return emptyList()
        return raw.split(SEPARATOR)
    }

    override fun save(ssids: List<String>) {
        prefs.edit {
            putString(KEY_SSIDS, ssids.joinToString(SEPARATOR))
            putBoolean(KEY_SEEDED, true)
        }
    }

    companion object {
        private const val PREFS_NAME = "allowed_networks"
        private const val KEY_SSIDS = "ssids"
        private const val KEY_SEEDED = "seeded"
        private const val SEPARATOR = "\n"
    }
}
