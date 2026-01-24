## Tecnologías y Arquitectura

El proyecto está desarrollado en **Kotlin** siguiendo los principios de **Clean Architecture** para asegurar la separación de responsabilidades.

### Stack Tecnológico
*   **Lenguaje:** Kotlin
*   **UI Toolkit:** Jetpack Compose Uses Material 3.
*   **Persistencia:** Repositorio personalizado (`CredentialsRepositoryImpl`).

## Cómo ejecutar el proyecto
Esta sección describe los pasos necesarios para configurar el entorno de desarrollo, instalar dependencias y ejecutar la aplicación.

### Prerequisitos

Antes de comenzar, asegúrate de cumplir con los siguientes requisitos en tu entorno local (Windows):

*   **Software de Desarrollo:**
    *   **Android Studio Otter** (2025.2.2) o superior.
    *   **Java Development Kit (JDK):** Versión 17 (recomendada para las últimas versiones del plugin de Gradle para Android) o al menos JDK 11.
    *   **Git:** Para el control de versiones.

*   **SDKs de Android:**
    *   Android SDK Platform 34 (UpsideDownCake) instalado a través del SDK Manager.
    *   SDK Build-Tools versión 34.0.0.

*   **Dispositivos:**
    *   Un emulador Android (AVD) configurado con API 24+ (Android 7.0 Nougat o superior).
    *   O un dispositivo físico Android con **Depuración por USB** habilitada en las *Opciones de desarrollador*.

*   **Permisos y Secretos:**
    *   **API Keys:** No se requieren claves de API externas (el almacenamiento es local).
    *   **Permisos:** El dispositivo debe tener conectividad a internet para que el `WebLoginActivity` funcione correctamente, aunque la compilación no lo requiere.

### Setup

Sigue estos pasos para configurar el proyecto en tu máquina:

1.  **Clonar el Repositorio:**
    Abre una terminal (PowerShell o Git Bash) y clona el proyecto:
    ```bash
    git clone https://github.com/DanielCarrenoMar/AutoLoginUCAB.git
    cd AutoLoginUCAB
    ```

2.  **Abrir en Android Studio:**
    *   Inicia Android Studio.
    *   Selecciona **File > Open** y navega hasta la carpeta raíz clonada.

3.  **Sincronización de Gradle:**
    *   Android Studio detectará automáticamente los archivos de construcción (`build.gradle.kts` o `build.gradle`).
    *   Si no inicia automáticamente, ve a **File > Sync Project with Gradle Files**.
    *   *Nota:* Este proceso descargará todas las dependencias de Jetpack Compose y Material 3. Espera a que la barra de progreso inferior finalice y aparezca "BUILD SUCCESSFUL" en la consola de Build.

4.  **Verificar Configuración SDK:**
    *   Asegúrate de que el archivo `local.properties` se haya generado automáticamente con la ruta correcta a tu `sdk.dir`.

### Running

Pasos para ejecutar la aplicación en modo desarrollo:

1.  **Seleccionar Configuración de Ejecución:**
    *   En la barra de herramientas superior de Android Studio, asegúrate de que el módulo seleccionado sea `app`.

2.  **Seleccionar Dispositivo:**
    *   En el menú desplegable de dispositivos, selecciona tu Emulador activo o tu dispositivo físico conectado (en caso de un dispositivo físico, deberá activar la opción de depuración en la sección de "Para desarrolladores" en la configuración del teléfono.).

3.  **Compilar y Ejecutar:**
    *   Haz clic en el botón verde **Run 'app'** (triángulo verde) o presiona `Shift` + `F10`.
    *   Gradle compilará el proyecto e instalará el APK en el dispositivo.

4.  **Verificación:**
    *   La aplicación debería abrirse automáticamente mostrando la pantalla principal (`MainActivity`) con el formulario de credenciales.
    *   Puedes monitorear logs y posibles errores en la pestaña **Logcat** filtrando por el paquete `com.app.autologinucab`.

## Estructura del Proyecto

```text
com.app.autologinucab
├── data
│   └── repository
│       └── CredentialsRepositoryImpl.kt   # Implementación concreta (Persistencia)
├── domain
│   ├── model
│   │   └── Credentials.kt                 # Data Class pura (Entidad)
│   ├── repository
│   │   └── CredentialsRepository.kt       # Interface del repositorio
│   └── usecase                            # Lógica de negocio atómica
│       ├── SaveCredentialsUseCase.kt
│       ├── GetCredentialsUseCase.kt
│       └── ClearCredentialsUseCase.kt
└── presentation
    ├── ui
    │   └── theme                          # Definiciones de estilos Compose
    ├── MainActivity.kt                    # Pantalla de configuración (CRUD)
    └── WebLoginActivity.kt                # Ejecutor del script de login (WebView)
```

## Lógica de Implementación
1. Inyección de Dependencias
El proyecto utiliza Inyección de Dependencias Manual en el punto de entrada de la actividad (MainActivity).
Ubicación: MainActivity.onCreate
Funcionamiento: Instancia el repositorio pasando el applicationContext y luego inyecta este repositorio en los casos de uso.

2. Gestión de Estado (UI)
La interfaz de usuario (MainActivity) utiliza Jetpack Compose y gestiona el estado localmente mediante remember y mutableStateOf.
Variables de visualización: savedUrl, savedUser, savedPassMasked (solo lectura, reflejan lo persistido).
Variables de entrada: url, username, password (vinculadas a los OutlinedTextField).
Sincronización: Se utiliza un bloque LaunchedEffect(Unit) para cargar los datos guardados al iniciar la actividad.

## Flujos de Usuario
- Flujo de Ejecución (Login)
- El usuario presiona "Ejecutar".
- MainActivity lanza un Intent explícito hacia WebLoginActivity.
- WebLoginActivity (no mostrada en este snippet, pero referenciada) se encarga de:
- Recuperar las credenciales.
- Cargar la URL en un WebView.
- Inyectar JavaScript para llenar el formulario y hacer submit.
- Flujo de Borrado
- El usuario presiona "Borrar".
- Se invoca ClearCredentialsUseCase.
- Se limpian las variables de estado locales (url, username, etc.) para reflejar el cambio inmediato en la UI.
