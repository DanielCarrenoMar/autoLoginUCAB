package com.app.autologinucab.data.remote

import android.net.Network
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class HttpPortalTransport(
    private val portalUrl: String = PortalContract.PORTAL_URL,
    private val connectTimeoutMillis: Int = 10_000,
    private val readTimeoutMillis: Int = 10_000,
    private val networkProvider: () -> Network? = { null }
) : PortalTransport {

    @Throws(IOException::class)
    override fun post(username: String, password: String): Int {
        val body = listOf(
            "buttonClicked" to "4",
            "err_flag" to "0",
            "redirect_url" to PortalContract.REDIRECT_URL,
            "username" to username,
            "password" to password
        ).joinToString("&") { (name, value) -> "${encode(name)}=${encode(value)}" }
        val bodyBytes = body.toByteArray(StandardCharsets.UTF_8)
        val url = URL(portalUrl)
        val boundNetwork = networkProvider()
        val connection = (
            boundNetwork?.openConnection(url) ?: url.openConnection()
            ) as HttpURLConnection

        return try {
            connection.requestMethod = "POST"
            connection.connectTimeout = connectTimeoutMillis
            connection.readTimeout = readTimeoutMillis
            connection.instanceFollowRedirects = true
            connection.useCaches = false
            connection.doOutput = true
            connection.setRequestProperty(
                "Content-Type",
                "application/x-www-form-urlencoded; charset=utf-8"
            )
            connection.setFixedLengthStreamingMode(bodyBytes.size)
            connection.outputStream.use { output -> output.write(bodyBytes) }
            connection.responseCode
        } finally {
            connection.disconnect()
        }
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())
}
