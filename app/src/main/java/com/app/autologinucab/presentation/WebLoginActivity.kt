package com.app.autologinucab.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import com.app.autologinucab.data.repository.CredentialsRepositoryImpl
import com.app.autologinucab.domain.usecase.GetCredentialsUseCase

class WebLoginActivity : ComponentActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repo = CredentialsRepositoryImpl(applicationContext)
        val getUseCase = GetCredentialsUseCase(repo)
        val credentials = getUseCase()

        val webView = WebView(this)
        setContentView(webView)

        if (credentials == null) {
            showMessage(webView, "No hay credenciales guardadas")
            return
        }

        val missing = buildList {
            if (credentials.url.isBlank()) add("link")
            if (credentials.username.isBlank()) add("usuario")
            if (credentials.password.isBlank()) add("contraseña")
        }

        if (missing.isNotEmpty()) {
            showMessage(
                webView,
                "Faltan datos guardados: ${missing.joinToString(", ")}. Guarda link/usuario/contraseña en la app."
            )
            return
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)

                val js = """
                    (function() {
                      function setValue(selector, value) {
                        var el = document.querySelector(selector);
                        if (!el) return false;
                        el.focus();
                        el.value = value;
                        el.dispatchEvent(new Event('input', { bubbles: true }));
                        el.dispatchEvent(new Event('change', { bubbles: true }));
                        return true;
                      }

                      var user = ${jsonString(credentials.username)};
                      var pass = ${jsonString(credentials.password)};

                      setValue('input[type=email]', user) ||
                      setValue('input[name*=user i]', user) ||
                      setValue('input[id*=user i]', user) ||
                      setValue('input[name*=login i]', user) ||
                      setValue('input[id*=login i]', user) ||
                      setValue('input[type=text]', user);

                      setValue('input[type=password]', pass) ||
                      setValue('input[name*=pass i]', pass) ||
                      setValue('input[id*=pass i]', pass);

                      var form = document.querySelector('form');
                      if (form) {
                        try {
                          // Intentar submit nativo
                          form.requestSubmit ? form.requestSubmit() : form.submit();
                          return JSON.stringify({ submitted: true });
                        } catch (e) {
                          return JSON.stringify({ submitted: false, error: String(e) });
                        }
                      }

                      return JSON.stringify({ submitted: false });
                    })();
                """.trimIndent()

                view.evaluateJavascript(js) { result ->
                    // result viene como string JSON, p.ej. "{\"submitted\":true}"
                    if (result != null && result.contains("\"submitted\":true")) {
                        // Cerrar la activity (cierra la pantalla con el WebView)
                        finish()
                    }
                }
            }
        }

        webView.loadUrl(credentials.url)
    }

    private fun showMessage(webView: WebView, message: String) {
        val html = """
            <html>
              <body style=\"font-family: sans-serif; padding: 16px;\">
                <h3>$message</h3>
              </body>
            </html>
        """.trimIndent()
        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
    }

    private fun jsonString(value: String): String {
        val escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
        return "\"$escaped\""
    }
}
