package com.app.autologinucab.data.remote

import java.io.IOException

interface PortalTransport {
    @Throws(IOException::class)
    fun post(username: String, password: String): Int
}
