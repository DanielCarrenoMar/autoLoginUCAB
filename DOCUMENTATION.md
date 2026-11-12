## Tecnologías y Arquitectura

El proyecto está desarrollado en **Kotlin** siguiendo los principios de **Clean Architecture** para asegurar la separación de responsabilidades.

### Stack Tecnológico

*   **Lenguaje:** Kotlin.
*   **UI Toolkit:** Jetpack Compose con Material 3.
*   **Persistencia:** `SharedPreferences` para el estado no sensible y `SecurePrefs` (AES-GCM + AndroidKeyStore) para las credenciales.
*   **Red:** `HttpURLConnection` con la petición ligada a la red Wi-Fi activa mediante `Network.openConnection`.
*   **Automatización:** callback de red del sistema (`registerNetworkCallback` con `PendingIntent`) que despierta la app y programa un `JobScheduler`; no hay servicio en primer plano ni notificación fija.
*   **Verificación de internet:** `NetworkCapabilities.NET_CAPABILITY_VALIDATED` con espera acotada.

### Estructura del Proyecto

```text
com.app.autologinucab
├── data
│   ├── local
│   │   ├── SecurePrefs.kt                          # Credenciales cifradas (AES-GCM/AndroidKeyStore)
│   │   ├── SharedPreferencesLoginStatusRepository.kt
│   │   ├── SharedPreferencesSsidSettingsStore.kt   # Lista persistente de SSID
│   │   └── WifiStateProvider.kt                    # Permiso, ubicación y SSID actual
│   ├── remote
│   │   ├── ConnectivityInternetValidator.kt        # Internet validada en la red usada
│   │   ├── HttpPortalTransport.kt                  # POST del portal, opcionalmente ligado al Wi-Fi
│   │   ├── PollingInternetValidator.kt             # Espera acotada y testeable
│   │   ├── PortalContract.kt                       # URL y redirect fijos
│   │   ├── PortalTransport.kt
│   │   └── WifiNetworkSelector.kt                  # Red Wi-Fi conectada
│   └── repository
│       ├── AllowedNetworksRepositoryImpl.kt
│       └── CredentialsRepositoryImpl.kt
├── di
│   └── AppContainer.kt                             # Grafo manual compartido
├── domain
│   ├── model
│   │   ├── Credentials.kt
│   │   ├── LoginResult.kt
│   │   ├── LoginTrigger.kt
│   │   └── SsidRules.kt                            # Normalización y redes por defecto
│   ├── repository
│   │   ├── AllowedNetworksRepository.kt
│   │   ├── CredentialsRepository.kt
│   │   ├── InternetValidator.kt
│   │   ├── LoginStatusRepository.kt
│   │   ├── SsidSettingsStore.kt
│   │   └── WifiStatusProvider.kt
│   └── usecase
│       ├── AutomaticLoginGate.kt                   # Permiso, ubicación, lista y SSID
│       ├── ClearCredentialsUseCase.kt
│       ├── GetCredentialsUseCase.kt
│       ├── HasCredentialsUseCase.kt
│       ├── LoginCoordinator.kt                     # Punto único + single-flight
│       ├── LoginExecutor.kt                        # POST + validación + resultado
│       ├── LoginSingleFlight.kt
│       ├── RepeatedConnectionGuard.kt
│       └── SaveCredentialsUseCase.kt
├── presentation
│   ├── MainActivity.kt                             # Estado, permisos y navegación
│   └── ui
│       ├── LoginResultMessages.kt                  # Resultado -> texto no sensible
│       ├── MainScreen.kt                           # Credenciales + acción principal
│       ├── MainScreenModels.kt
│       ├── NetworksScreen.kt                       # Lista de SSID permitidos
│       └── theme
├── service
│   ├── AutomationTrigger.kt                        # Callback Wi-Fi del sistema (PendingIntent)
│   ├── LoginJobScheduler.kt                        # Programa el intento como job
│   ├── LoginJobService.kt                          # Ejecuta el intento en segundo plano
│   ├── LoginNotifications.kt                       # Avisos de conectar/conectado/error
│   ├── NetworkTriggerReceiver.kt                   # Recibe el Wi-Fi disponible
│   └── BootReceiver.kt
└── tile
    └── AutoLoginTileService.kt                     # Mosaico de ajustes rápidos
```

## Flujo de Login Directo

1. El usuario pulsa **Conectar ahora** (o el mosaico de ajustes rápidos).
2. `MainActivity` valida usuario y contraseña, conserva el valor cifrado anterior si el campo de contraseña queda vacío, guarda los cambios y programa el intento con `LoginJobScheduler`; `LoginJobService` lo ejecuta en segundo plano.
3. `LoginCoordinator` adquiere el `LoginSingleFlight`; si ya hay un intento en curso devuelve `null` sin enviar nada.
4. `LoginExecutor` registra `RUNNING` en el estado duradero, lee las credenciales cifradas y llama a `HttpPortalTransport`.
5. `HttpPortalTransport` envía el formulario exacto:
   `buttonClicked=4&err_flag=0&redirect_url=http://www.google.com&username=...&password=...`
   con `Content-Type: application/x-www-form-urlencoded; charset=utf-8`, ligado a la red Wi-Fi conectada.
6. Si el portal responde `2xx`, `ConnectivityInternetValidator` espera hasta 15 segundos a que Android marque esa red como validada.
7. El resultado se guarda sin datos sensibles (`SUCCESS`, `NO_INTERNET`, `HTTP_ERROR`, `NETWORK_ERROR`, `TIMEOUT`, etc.) y se muestra en la pantalla principal, en el mosaico y en un aviso de notificación que se puede deslizar para quitar de la barra.

## Flujo Automático

1. `AutomationTrigger` registra en el sistema un `NetworkCallback` para redes Wi-Fi mediante un `PendingIntent`; la app no mantiene ningún servicio en primer plano y el registro se renueva al abrir la app, al reiniciar el teléfono y al actualizar la app.
2. Cuando hay una red Wi-Fi disponible, el sistema envía el broadcast a `NetworkTriggerReceiver`, que programa un intento automático con `LoginJobScheduler`.
3. `LoginJobService` lee el SSID, lo normaliza (solo quita las comillas que agrega Android) y comprueba que esté en la lista permitida; si no lo está, termina sin avisar ni enviar credenciales.
4. `RepeatedConnectionGuard` evita repetir el intento para la misma conexión dentro de 60 segundos.
5. `AutomaticLoginGate` verifica permiso de ubicación, ubicación activa, lista no vacía y SSID permitido. Si algo falla, guarda el motivo (`PERMISSION_REQUIRED`, `LOCATION_DISABLED`, `EMPTY_SSID_LIST`, `SSID_NOT_ALLOWED`) y no envía credenciales.
6. El botón manual y el mosaico **no** pasan por el filtro de SSID: son acciones explícitas y siguen disponibles en cualquier red.

## Reglas de Estado

- `RUNNING` se escribe al empezar un intento. Si el proceso muere, la pantalla lo reemplaza por `INTERRUPTED` para que el botón no quede deshabilitado para siempre.
- El estado se guarda en `SharedPreferences` (`login_status`) y nunca incluye usuario, contraseña, SSID ni cuerpos de respuesta.
- Los errores HTTP conservan solo el código de estado.
- La lista de SSID distingue "nunca configurada" de "vacía a propósito": la primera siembra las cuatro redes por defecto, la segunda no se vuelve a sembrar.

## Interfaz

- **Pantalla principal:** edición de usuario y contraseña, botón grande **Conectar ahora**, estado del último intento y aviso del portal fijo. La gestión de redes queda como acción secundaria en la barra superior.
- **Redes permitidas:** lista actual, alta y baja de SSID, mensaje de error para vacíos o duplicados y explicación de que una lista vacía desactiva la automatización.
- **Permisos:** tarjeta explicativa con acción para conceder el permiso de ubicación o abrir los ajustes de ubicación. En Android 13+ la app pide el permiso de notificaciones al abrirse, porque sin él no se ven los avisos de conexión.
- **Contraseña:** siempre enmascarada y nunca se rellena con el valor descifrado; dejar el campo vacío conserva la contraseña guardada.

## Cómo ejecutar el proyecto

### Prerequisitos

*   **Android Studio Otter** (2025.2.2) o superior.
*   **JDK 17 o superior** (el proyecto compila con JDK 21 en la máquina de desarrollo).
*   **Android SDK Platform 36** y **Build-Tools 36.0.0**.
*   Un emulador o dispositivo con Android 8.0 (API 26) o superior.

### Setup

1.  Clona el repositorio:
    ```bash
    git clone https://github.com/DanielCarrenoMar/AutoLoginUCAB.git
    cd AutoLoginUCAB
    ```
2.  Abre la carpeta raíz en Android Studio.
3.  Sincroniza Gradle (`File > Sync Project with Gradle Files`).
4.  Verifica que `local.properties` apunte a tu SDK (`sdk.dir`).

### Running

*   Selecciona el módulo `app` y un dispositivo.
*   Pulsa **Run 'app'** (`Shift` + `F10`).
*   La pantalla principal abre con el formulario de credenciales.

### Verificación

```bash
./gradlew :app:testDebugUnitTest          # pruebas JVM
./gradlew :app:assembleDebug              # compilación de la app
./gradlew :app:compileDebugAndroidTestKotlin  # compilación de pruebas Compose
./gradlew :app:lintDebug                  # lint de Android
```

Las pruebas instrumentadas de Compose (`MainScreenTest`, `NetworksScreenTest`) requieren un emulador o dispositivo:

```bash
./gradlew :app:connectedDebugAndroidTest
```

## Limitaciones conocidas

- El portal se consume por **HTTP** en la dirección fija indicada; no hay TLS ni forma de configurar otro destino desde la interfaz.
- La lectura del SSID depende del permiso de ubicación y de que la ubicación del sistema esté activa. Android puede endurecer el acceso en segundo plano según la versión; la app vuelve a comprobar al abrirse y en cada evento de red.
- La automatización ya no mantiene un servicio en primer plano: el sistema despierta la app con un callback de red y ejecuta el intento como job. Por eso no hay notificación fija; solo aparecen avisos al conectar, al conectarse y cuando hay un error, y todos se pueden deslizar para quitar.
- Como el intento lo programa Android, normalmente arranca al instante de detectar el Wi-Fi, pero con ahorro de energía activo o la app en un bucket de uso restringido puede demorar unos minutos. El registro del callback se renueva al abrir la app, al reiniciar el teléfono y al actualizar la app.
- No hay acción para restaurar la lista de SSID por defecto después del primer uso.
