package com.app.autologinucab.data.remote

import java.net.InetAddress
import java.net.ServerSocket
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.concurrent.thread
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HttpPortalTransportTest {

    @Test
    fun sendsExactUrlEncodedPortalForm() {
        val capture = captureRequest(responseStatus = 200) { endpoint ->
            HttpPortalTransport(endpoint).post("user+@ucab", "p&= ss\"")
        }

        assertEquals(200, capture.responseStatus)
        assertEquals("POST /login.html HTTP/1.1", capture.requestLine)
        assertEquals("application/x-www-form-urlencoded; charset=utf-8", capture.contentType)

        val expectedBody = "buttonClicked=4&err_flag=0&" +
            "redirect_url=http%3A%2F%2Fwww.google.com&" +
            "username=user%2B%40ucab&password=p%26%3D+ss%22"
        assertEquals(expectedBody, capture.body)
        assertEquals(
            mapOf(
                "buttonClicked" to "4",
                "err_flag" to "0",
                "redirect_url" to "http://www.google.com",
                "username" to "user+@ucab",
                "password" to "p&= ss\""
            ),
            decodeForm(capture.body)
        )
    }

    @Test
    fun returnsPortalHttpFailureForExecutorToClassify() {
        val capture = captureRequest(responseStatus = 403) { endpoint ->
            HttpPortalTransport(endpoint).post("user", "pass")
        }

        assertEquals(403, capture.responseStatus)
    }

    @Test
    fun productionContractUsesTheSuppliedPortalAndRedirect() {
        assertEquals("http://192.0.2.1/login.html", PortalContract.PORTAL_URL)
        assertEquals("http://www.google.com", PortalContract.REDIRECT_URL)
    }

    private fun captureRequest(
        responseStatus: Int,
        request: (String) -> Int
    ): CapturedRequest {
        val server = ServerSocket(0, 1, InetAddress.getLoopbackAddress())
        var requestLine = ""
        var contentType = ""
        var body = ""
        var transportFailure: Throwable? = null
        val worker = thread(name = "local-portal-test") {
            try {
                server.accept().use { socket ->
                    val input = socket.getInputStream().bufferedReader(StandardCharsets.ISO_8859_1)
                    requestLine = input.readLine()
                    var contentLength = 0
                    while (true) {
                        val line = input.readLine()
                        if (line.isEmpty()) break
                        val name = line.substringBefore(':').lowercase()
                        val value = line.substringAfter(':', "").trim()
                        when (name) {
                            "content-type" -> contentType = value
                            "content-length" -> contentLength = value.toInt()
                        }
                    }
                    val bodyChars = CharArray(contentLength)
                    var read = 0
                    while (read < contentLength) {
                        val count = input.read(bodyChars, read, contentLength - read)
                        if (count < 0) break
                        read += count
                    }
                    body = String(bodyChars, 0, read)
                    val response = "HTTP/1.1 $responseStatus Test\r\n" +
                        "Content-Length: 0\r\nConnection: close\r\n\r\n"
                    socket.getOutputStream().apply {
                        write(response.toByteArray(StandardCharsets.ISO_8859_1))
                        flush()
                    }
                }
            } catch (failure: Throwable) {
                if (!server.isClosed) transportFailure = failure
            }
        }
        val endpoint = "http://${server.inetAddress.hostAddress}:${server.localPort}/login.html"
        var actualStatus = -1
        try {
            actualStatus = request(endpoint)
            worker.join(5_000)
        } finally {
            server.close()
            worker.join(1_000)
        }
        transportFailure?.let { throw it }
        assertFalse("Local server did not finish", worker.isAlive)
        return CapturedRequest(actualStatus, requestLine, contentType, body)
    }

    private fun decodeForm(body: String): Map<String, String> =
        body.split('&').associate { field ->
            val (name, value) = field.split('=', limit = 2)
            decode(name) to decode(value)
        }

    private fun decode(value: String): String =
        URLDecoder.decode(value, StandardCharsets.UTF_8.name())

    private data class CapturedRequest(
        val responseStatus: Int,
        val requestLine: String,
        val contentType: String,
        val body: String
    )
}
