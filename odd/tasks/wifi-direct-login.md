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

- [x] T1 (S1, S2) — inline — Direct form transport, bounded validation policy, durable non-sensitive result, and deterministic unit tests. Commit: ddfa2b5.
- [x] T2 (S4) — inline — Persistent allowed-SSID repository with first-use defaults and focused tests. Commit: de0fc0f.
- [x] T3 (S1, S6, S7, S8) — inline — Shared reliable background execution, Wi-Fi-bound transport, ConnectivityManager Internet validation, quick-settings tile entry point, network callback, SSID gate, and permission declarations. Commit: 64ebd98.
- [x] T4 (S3, S4, S5, S7, S8) — inline — Redesigned state-driven Compose screens, credential editing, prominent login action, secondary SSID management, permission explanation, and UI/unit tests. Commit: b6acb3c.
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

L5 — 2026-11-12 T2 evidence: added `SsidRules` (quote-only normalization, four exact defaults), the `AllowedNetworksRepository` port, a pure `AllowedNetworksRepositoryImpl` with first-use seeding through `SsidSettingsStore`, and the SharedPreferences store that distinguishes "never configured" from an intentionally empty list. JVM tests cover seeding, persisted reads, an empty list, add/duplicate/blank, removal to empty, and quote-only matching. They were written test-first but could not be executed: the machine no longer has a JDK, Android SDK, or Gradle cache (`java`, `JAVA_HOME`, `ANDROID_HOME`, `app/build`, and `.gradle` are all absent). The user explicitly chose to continue without compiling, so RED/GREEN evidence is unavailable and every check is reported as not run.

L6 — 2026-11-12 T3 evidence: added `LoginTrigger`, `AutomaticLoginGate` (permission, location, empty-list, exact-SSID checks), `LoginSingleFlight`, `LoginCoordinator` (manual/tile bypass the gate; automatic blocked results are persisted), and `RepeatedConnectionGuard`. `HttpPortalTransport` now accepts an optional bound `Network`; `WifiNetworkSelector` picks the connected Wi-Fi network; `ConnectivityInternetValidator` polls Android's `NET_CAPABILITY_VALIDATED` for the bound network (or the active network for manual fallback) through the testable `PollingInternetValidator`. `WifiStateProvider` centralizes SSID permission and location handling. `AutoLoginService` is a `specialUse` foreground service with a Wi-Fi network callback, a single worker, and the same coordinator; `BootReceiver` restarts it after boot or update; the tile now enqueues `LoginTrigger.TILE` instead of opening `WebLoginActivity`. Manifest declares network, Wi-Fi, location, nearby-Wi-Fi, foreground-service, boot and notification permissions. Decision: the allowed-SSID gate applies only to automatic triggers, because S7 keeps the manual button and tile available on any network. Tests were added but not executed, for the same missing-toolchain reason as L5.

Next: T5. T2 was committed as de0fc0f (`feat(networks): persist allowed Wi-Fi networks with defaults`); T3 was committed as 64ebd98 (`feat(login): run direct login in the background with Wi-Fi gating`); T4 was committed as b6acb3c (`feat(ui): redesign the main screen and add allowed-network management`).

L7 — 2026-11-12 T5 evidence: deleted `WebLoginActivity` and its manifest entry, so no WebView or JavaScript login path remains. `MainActivity` now goes through the existing `GetCredentialsUseCase`, `SaveCredentialsUseCase` and `ClearCredentialsUseCase`, which `AppContainer` exposes next to the repositories; the activity no longer calls the repository directly. Rewrote `README.md` and `DOCUMENTATION.md` for the direct POST, permissions, background service, SSID rules, fixed portal, transport limitation and verification commands. Checks available without a JDK/SDK: every resource XML parses, every `R.string` reference resolves to a declared string with no unused strings, `git diff --check` reports no whitespace errors, and no WebView/WebLoginActivity reference remains outside the historical log. Gradle unit tests, compilation, lint and Compose instrumentation remain not run because the toolchain is absent.
