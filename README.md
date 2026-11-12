<a>
    <img src="https://github.com/DanielCarrenoMar/Snake-XPR_UCAB/assets/144462396/d30c8055-4d82-4a05-b0f3-5f74c85ffb7f" alt="Logo" title="Logo" align="right" height="70" />
</a>

# ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=Kotlin&logoColor=white) AutoLoginUCAB

![platform](https://img.shields.io/badge/platform-Android-brightgreen.svg)

Inicia sesión en el portal Wi-Fi de la UCAB Guayana sin abrir un navegador. La app guarda tus credenciales cifradas en el teléfono y envía el formulario del portal directamente cuando te conectas a una de tus redes permitidas.

> Probado en Android 12+

## Cómo funciona

- Envía `POST application/x-www-form-urlencoded` a `http://192.0.2.1/login.html` con `buttonClicked=4`, `err_flag=0`, `redirect_url=http://www.google.com`, tu usuario y tu contraseña. Es el mismo formulario que usaba el script original.
- No abre un WebView ni ejecuta JavaScript.
- Después del envío espera a que Android valide internet y distingue entre fallo del portal, petición aceptada sin internet y conexión validada.
- Las credenciales se cifran con AES-GCM y una clave del AndroidKeyStore.

## Cómo instalar ⚙️

- Accede al [último lanzamiento](https://github.com/DanielCarrenoMar/autoLoginUCAB/releases).
- Instala el APK (ten en cuenta que, al ser una instalación externa de la Play Store, te pedirá activar una configuración del teléfono. [Info](https://support.google.com/googleplay/thread/216257040/quiero-habilitar-el-acceso-de-fuentes-desconocidas?hl=es))
- Abre la app instalada.

## Cómo usar 📲

1. Guarda tu usuario y contraseña. La contraseña queda cifrada y no se vuelve a mostrar.
2. Revisa la lista de redes permitidas: trae cuatro redes por defecto (`Estudiantes.Ingenierias`, `Empleados.Ingenierias`, `Empleados.Mod_AR_IV`, `Estudiantes.Mod_AR_IV`) y puedes agregar o quitar SSID.
3. Concede el permiso de ubicación: Android lo exige para leer el nombre del Wi-Fi. Si la ubicación del teléfono está apagada, la app te lo indica y te lleva a los ajustes.
4. Pulsa **Conectar ahora** para ejecutar el login en ese momento.
5. La automatización también se ejecuta al conectarte a una red permitida, aunque la app esté cerrada. Puedes forzar un intento desde el mosaico de ajustes rápidos (**AutoLogin**).

## Permisos

- **Ubicación**: leer el SSID de la red actual. Android no entrega el nombre del Wi-Fi sin este permiso.
- **Wi-Fi / red**: consultar el estado de la conexión y enviar la petición por la red Wi-Fi activa.
- **Notificaciones**: mostrar los avisos de conexión (al conectar, al conectarse y cuando hay un error), Android 13+. La app pide este permiso al abrirse; si lo niegas, la automatización sigue funcionando sin avisos visibles.
- **Inicio**: reactivar la automatización después de reiniciar el teléfono.
- **Red**: el callback de Wi-Fi lo registra el sistema, así que la app ya no necesita el permiso de servicio en primer plano.

## Notas importantes

- El portal usa **HTTP sin cifrar**. Las credenciales viajan dentro de la red del portal; la app no puede cambiar el transporte que exige el servidor.
- La URL del portal es fija (`http://192.0.2.1/login.html`) porque el `POST` suministrado está atado a ese destino. El campo de URL anterior se conserva en el almacenamiento cifrado solo por compatibilidad y ya no se usa.
- Si la lista de redes permitidas queda vacía, la automatización no se ejecuta hasta que agregues una. La app no vuelve a sembrar las redes por defecto después del primer uso.
- La automatización no usa un servicio en primer plano: el sistema avisa a la app cuando aparece una red Wi-Fi y el intento se ejecuta como job. Si Android no entrega el SSID en segundo plano en tu versión, abre la app una vez y la comprobación se repite.
- Las notificaciones son solo para eventos (conectar, conectado, error) y se pueden deslizar para quitar de la barra en cualquier momento; no queda ningún aviso fijo de «automatización activa».

## Características ⭐

- Inicio de sesión directo por `POST`, sin navegador ni JavaScript.
- Credenciales cifradas en el almacenamiento local.
- Edición de usuario y contraseña desde la pantalla principal.
- Lista de SSID permitidos editable y persistente.
- Ejecución automática en segundo plano al conectarse a una red permitida.
- Verificación real de internet con el estado de red validada de Android.
- Mosaico de ajustes rápidos y resultado duradero sin datos sensibles.
- Avisos de conexión descartables: sin notificación fija ni servicio permanente.

## Documentación 📖

Se puede visualizar [aquí](DOCUMENTATION.md).

### Imagenes
<div>
  <img width="270" height="560" alt="dark" src="https://github.com/user-attachments/assets/edfc8756-c221-426f-89c7-e85e8c7f1ab5" />
  <img width="270" height="560" alt="light" src="https://github.com/user-attachments/assets/78addadd-4c14-4297-829f-efa869af04af" />
  <img width="500" height="560" alt="mosaic" src="https://github.com/user-attachments/assets/66939c51-e22f-49d4-b827-f25e149a142e" />
</div>
