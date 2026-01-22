package com.app.autologinucab.presentation

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.app.autologinucab.data.repository.CredentialsRepositoryImpl
import com.app.autologinucab.domain.model.Credentials
import com.app.autologinucab.domain.usecase.ClearCredentialsUseCase
import com.app.autologinucab.domain.usecase.GetCredentialsUseCase
import com.app.autologinucab.domain.usecase.SaveCredentialsUseCase
import com.app.autologinucab.presentation.ui.theme.AutoLoginUCABTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repo = CredentialsRepositoryImpl(applicationContext)
        val saveUseCase = SaveCredentialsUseCase(repo)
        val getUseCase = GetCredentialsUseCase(repo)
        val clearUseCase = ClearCredentialsUseCase(repo)

        setContent {
            AutoLoginUCABTheme {
                var username by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var url by remember { mutableStateOf("") }
                var status by remember { mutableStateOf("") }

                // Valores guardados (para mostrar)
                var savedUrl by remember { mutableStateOf<String?>(null) }
                var savedUser by remember { mutableStateOf<String?>(null) }
                var savedPassMasked by remember { mutableStateOf<String?>(null) }

                fun refreshSaved() {
                    val c = getUseCase()
                    savedUrl = c?.url
                    savedUser = c?.username
                    savedPassMasked = c?.password?.takeIf { it.isNotEmpty() }?.let { "*".repeat(it.length.coerceAtMost(16)) }
                }

                // Cargar al inicio
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    refreshSaved()
                }

                val context = LocalContext.current

                Scaffold(
                    topBar = { TopAppBar(title = { Text("AutoLoginUCAB") }) }
                ) { padding: PaddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Guardado:")
                        Text("Link: ${savedUrl ?: "(vacío)"}")
                        Text("Usuario: ${savedUser ?: "(vacío)"}")
                        Text("Contraseña: ${savedPassMasked ?: "(vacío)"}")

                        Row {
                            TextButton(onClick = {
                                clearUseCase()
                                url = ""
                                username = ""
                                password = ""
                                refreshSaved()
                                status = "Datos borrados"
                            }) { Text("Borrar", color = MaterialTheme.colorScheme.secondary) }
                        }

                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text("URL del formulario") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Usuario") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            singleLine = true
                        )

                        Row (modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(onClick = {
                                // Lanzar la actividad WebLoginActivity
                                context.startActivity(Intent(context, WebLoginActivity::class.java))
                            }) {
                                Text("Ejecutar")
                            }

                            Button(onClick = {
                                val existing = getUseCase()

                                val newUrl = url.trim()
                                val newUser = username.trim()
                                val newPass = password

                                // Solo reemplazar valores si el input NO está vacío
                                val finalUrl = newUrl.ifBlank { (existing?.url ?: "") }
                                val finalUser = newUser.ifBlank { (existing?.username ?: "") }
                                val finalPass = newPass.ifBlank { (existing?.password ?: "") }

                                // Si no hay nada nuevo escrito, no hacemos escritura innecesaria
                                if (newUrl.isBlank() && newUser.isBlank() && newPass.isBlank()) {
                                    status = "No hay cambios para guardar (todos los inputs están vacíos)"
                                    return@Button
                                }

                                saveUseCase(
                                    Credentials(
                                        username = finalUser,
                                        password = finalPass,
                                        url = finalUrl
                                    )
                                )
                                refreshSaved()
                                status = "Guardado"

                                // Limpiar inputs después de guardar
                                url = ""
                                username = ""
                                password = ""
                            }) {
                                Text("Guardar")
                            }

                        }

                        if (status.isNotBlank()) Text(status)

                        Text("Nota: el Tile se habilita desde Editar mosaicos en Ajustes rápidos.")
                    }
                }
            }
        }
    }
}
