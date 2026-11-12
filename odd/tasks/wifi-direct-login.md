# Wi-Fi direct login
Objective: replace WebView login with the supplied direct POST, automate it on allowed Wi-Fi networks, and simplify the Compose UI.
Branch: main. Delivery: single-pr (local work-unit commits only; no push or PR without user approval). Reviewed boundary: b66221b4dce54113a2d6e281506985aa29653c6e.
Test runner: Gradle through the gradle-run skill; JUnit4 plus AndroidX/Compose tests where platform behavior requires them.

## Specs

### S1 — Direct request instead of browser automation
User requirement, verbatim:
> La aplicacion actualmente se le tiene que dar a un boton para que se inicie y funciona abriendo un navegador y colocando la informacion con js
>
> quiero en que vez de eso maneje pedir el permiso para automatizar el proceso ejecutando un script que manda un post con tus datos directamente el servidor para hacer el login en la wifi

Authorized scope: reproduce the request natively on Android without PowerShell, WebView, or browser UI. Preserve the current encrypted credential source. Use HTTP only for the user-supplied portal and disclose its transport limitation.

### S2 — Exact portal contract
User requirement, verbatim:
> el psot que se debe enviar al servidor es el mismo que usa
>
> $portalUrl = "http://192.0.2.1/login.html"
> $redirectUrl = "http://www.google.com"
>
> $body = @{
>     buttonClicked = "4"
>     err_flag      = "0"
>     redirect_url  = $redirectUrl
>     username      = $username
>     password      = $password
> }
>
> try {
>     $response = Invoke-WebRequest -Uri $portalUrl -Method Post -Body $body -UseBasicParsing 

Acceptance: POST `application/x-www-form-urlencoded` to `http://192.0.2.1/login.html`, with exactly `buttonClicked=4`, `err_flag=0`, `redirect_url=http://www.google.com`, and the saved username/password. Encode form values correctly. Do not log credentials. Exercise success, HTTP failure, transport failure, and timeout through a deterministic fake transport.

### S3 — UI improvement and encrypted editable credentials
User requirement, verbatim:
> Mejora la interfaz de la app actual
>
> la app debe poder
> - guardar usuario y contraseña de forma cifrada como hace actualmente
> - permitirte cambiar el usuario y contraseña

Acceptance: retain AndroidKeyStore AES-GCM encryption and existing stored values; allow both fields to be replaced; mask password input; never display the decrypted password. Validate blank credentials before saving or running.

### S4 — SSID list management and defaults
User requirement, verbatim:
> - Mostrar la lista de los wifis al que se le aplica la automatizacion
> - Permitirte agregar o eliminar SSID de wifis a esa lista
> - tener una lista de wifis por defecto: Estudiantes.Ingenierias,Empleados.Ingenierias,Empleados.Mod_AR_IV,Estudiantes.Mod_AR_IV

Acceptance: persist additions/removals, seed exactly those four SSIDs on first use, preserve an intentionally empty list, show the current list, and add/remove entries through the public UI. Match SSIDs exactly after removing only Android's surrounding quotes.

### S5 — Main screen priority
User requirement, verbatim:
> La parte principal debe ser el cambiar los datos de usuario y contraseña y ejecutar el script directamente con un boton en el medio
>
> ya con menor importacion acceder a la lista para ver y modificar los wifis que desencadenan el script

Acceptance: the main screen centers credential editing and one prominent login action; SSID management is secondary navigation; expose progress and a non-sensitive last result.

### S6 — Existing shortcut
User requirement, verbatim:
> El atajo que tiene actualmente la app debe sergui funcionando pero en vez de abrir un navegador ejecutara el script

Acceptance: the existing quick-settings tile enqueues the same direct-login operation, does not open WebLoginActivity, and reports failure through durable non-sensitive status rather than a result notification.

### S7 — Automatic background execution
User decision, verbatim:
> Automática en segundo plano (Recommended)

Authorized scope: on connection to an allowed SSID, execute even when the app UI is closed; manual button and tile remain available. Request the location permissions needed for SSID access, explain why, handle denied permissions and disabled location without crashing, and avoid repeated concurrent attempts for the same connection.

### S8 — Internet verification
User decision, verbatim:
> Verificar internet

Acceptance: after the POST, check Android's validated-network state with a bounded wait. Distinguish request failure from request-completed-but-no-Internet and Internet validated. Do not treat HTTP 200 alone as Internet success.

### S9 — Additional suggestions not selected
Only `Verificar internet` was selected. Do not add result notifications or a restore-defaults action in this feature. Defaults still seed initially as required by S4.

## Tasks

- [ ] T1 (S1, S2) — inline — Direct form transport, bounded validation policy, durable non-sensitive result, and deterministic unit tests. Commit: ddfa2b5.
- [ ] T2 (S4) — inline — Persistent allowed-SSID repository with first-use defaults and focused tests. Commit: pending.
- [ ] T3 (S1, S6, S7, S8) — inline — Shared reliable background execution, Wi-Fi-bound transport, ConnectivityManager Internet validation, quick-settings tile entry point, network callback, SSID gate, and permission declarations. Commit: pending.
- [ ] T4 (S3, S4, S5, S7, S8) — inline — Redesigned state-driven Compose screens, credential editing, prominent login action, secondary SSID management, permission explanation, and UI/unit tests. Commit: pending.
- [ ] T5 (S1, S2, S3, S4, S5, S6, S7, S8) — inline — Retire WebView flow, update user documentation/help, run all applicable checks, and assess the final work unit. Commit: pending.

Route evidence: all tasks remain inline because no parallel independent units are being launched and the context backstop has not fired. File count and estimated size are not delegation triggers.
Delivery forecast: approximately 1,000–1,400 authored changed lines including tests and docs; generated files excluded. Strategy: `single-pr`; no chained PRs or remote delivery are authorized.

## Log

L1 — Original user request, verbatim:
> La aplicacion actualmente se le tiene que dar a un boton para que se inicie y funciona abriendo un navegador y colocando la informacion con js
>
> quiero en que vez de eso maneje pedir el permiso para automatizar el proceso ejecutando un script que manda un post con tus datos directamente el servidor para hacer el login en la wifi
>
> el psot que se debe enviar al servidor es el mismo que usa
>
> $portalUrl = "http://192.0.2.1/login.html"
> $redirectUrl = "http://www.google.com"
>
> $body = @{
>     buttonClicked = "4"
>     err_flag      = "0"
>     redirect_url  = $redirectUrl
>     username      = $username
>     password      = $password
> }
>
> try {
>     $response = Invoke-WebRequest -Uri $portalUrl -Method Post -Body $body -UseBasicParsing 
>
> Mejora la interfaz de la app actual
>
> la app debe poder
> - guardar usuario y contraseña de forma cifrada como hace actualmente
> - permitirte cambiar el usuario y contraseña
> - Mostrar la lista de los wifis al que se le aplica la automatizacion
> - Permitirte agregar o eliminar SSID de wifis a esa lista
> - tener una lista de wifis por defecto: Estudiantes.Ingenierias,Empleados.Ingenierias,Empleados.Mod_AR_IV,Estudiantes.Mod_AR_IV
>
> La parte principal debe ser el cambiar los datos de usuario y contraseña y ejecutar el script directamente con un boton en el medio
>
> ya con menor importacion acceder a la lista para ver y modificar los wifis que desencadenan el script
>
>
> El atajo que tiene actualmente la app debe sergui funcionando pero en vez de abrir un navegador ejecutara el script
>
> has todas las preguntas necesarias y realiza 3 suguerencias adicionales

L2 — 2026-11-11 user decisions, verbatim:
> Automática en segundo plano (Recommended)
>
> Verificar internet

L3 — Exploration evidence: WebLoginActivity fills and submits a WebView form using JavaScript; no direct HTTP implementation or SSID storage exists. SecurePrefs already encrypts username/password/url with AES-GCM and AndroidKeyStore. The quick-settings tile currently opens WebLoginActivity. Manifest currently allows cleartext traffic and declares only INTERNET. Parent spot-checked WebLoginActivity.kt:1-129.

L4 — 2026-11-12 T1 evidence: initial environment required the installed JDK 21, ignored local.properties for the Android SDK, disk cleanup by the user, and lower Gradle memory. The pre-existing missing tile drawable blocked resource linking and was added. Baseline `:app:testDebugUnitTest` then passed. New tests initially failed to compile for missing production types (RED); after implementation the complete debug JVM suite passed, followed by focused `HttpPortalTransportTest` and `LoginExecutorTest` passing. A JVM crash produced untracked `app/hs_err_pid1336.log`; it was not read or included because full runtime logs are sensitive artifacts. Platform Wi-Fi binding and ConnectivityManager validation moved to T3 because they require network permissions and a concrete Wi-Fi network; S8 remains open.

Next: T2. T1 was committed directly on main as ddfa2b5 (`feat(login): add direct portal request and result tracking`).
