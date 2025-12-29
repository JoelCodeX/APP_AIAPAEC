# AIAPAEC - Aplicación Móvil Android

## 📱 Descripción del Proyecto

AIAPAEC es una aplicación móvil nativa para Android desarrollada en **Kotlin** y **Jetpack Compose**. Su función principal es facilitar la gestión académica y la **calificación automática de exámenes** mediante escaneo de cartillas de respuestas (OMR).

La aplicación permite a los docentes y administradores gestionar exámenes, ver resultados en tiempo real y digitalizar notas de manera eficiente.

## ✨ Funcionalidades Principales

- **📸 Escaneo OMR Inteligente**:
  - Captura de cartillas de examen usando **CameraX**.
  - Detección de bordes y recorte automático.
  - Envío seguro al backend para procesamiento y calificación instantánea.
  - Visualización de resultados con overlay (superposición de respuestas correctas/incorrectas).
- **📝 Gestión de Exámenes**:
  - Listado de exámenes programados y pasados.
  - Aplicación de exámenes y asignación de notas.
- **📊 Registro de Notas**:
  - Visualización de notas por alumno y sección.
  - Formatos semanales y bimestrales.
- **🔐 Acceso Seguro**:
  - Autenticación JWT integrada.
  - Perfiles de usuario y gestión de sesión.
- **🏫 Gestión Académica**:
  - Listado de estudiantes por sección.
  - Visualización de detalles del estudiante.

## 🏗️ Arquitectura y Tecnologías

El proyecto sigue una arquitectura **MVVM (Model-View-ViewModel)** limpia y modular.

### Stack Tecnológico
- **Lenguaje**: Kotlin 2.2.20
- **UI Toolkit**: Jetpack Compose (Material Design 3)
- **Navegación**: Navigation Compose
- **Red**: Retrofit 2 + OkHttp + Gson
- **Cámara**: CameraX + Accompanist Permissions
- **Imágenes**: Coil (carga asíncrona) + OpenCV (procesamiento base)
- **Inyección de Dependencias**: Hilt (preparado/en integración)
- **Corrutinas**: Kotlin Coroutines & Flow

### Estructura del Proyecto (`app/src/main/java/com/jotadev/aiapaec`)

```
com.jotadev.aiapaec/
├── data/                  # Capa de Datos
│   ├── api/               # Interfaces Retrofit y cliente HTTP
│   ├── mappers/           # Transformadores de DTO a Dominio
│   ├── repository/        # Implementación de repositorios
│   └── storage/           # Persistencia local (Token, User Prefs)
├── domain/                # Capa de Dominio
│   ├── models/            # Data classes de negocio
│   ├── repository/        # Interfaces de repositorios
│   └── usecases/          # Casos de uso (Lógica de negocio pura)
├── di/                    # Inyección de Dependencias (AppModule)
├── navigation/            # Rutas y grafos de navegación
├── ui/                    # Capa de Presentación (Compose)
│   ├── components/        # UI Reutilizable (TopBar, Shimmer, etc.)
│   ├── theme/             # Sistema de diseño (Color, Type, Theme)
│   └── screens/           # Pantallas por funcionalidad
│       ├── login/         # Autenticación
│       ├── home/          # Dashboard principal
│       ├── scan/          # Escaneo y resultados OMR
│       ├── exams/         # Listado y aplicación de exámenes
│       ├── grades/        # Registro de notas
│       ├── students/      # Directorio de estudiantes
│       ├── settings/      # Configuración de usuario
│       └── format/        # Formatos semanales
└── utils/                 # Utilidades generales
```

## 📋 Requisitos de Desarrollo

- **Android Studio**: Ladybug o superior.
- **JDK**: Versión 17 o 21.
- **Dispositivo**: Android 7.1 (API 25) mínimo. Recomendado Android 10+.

## 🚀 Instalación y Ejecución

1.  **Clonar el repositorio**:
    ```bash
    git clone <url-repo>
    cd FRONTED
    ```
2.  **Abrir en Android Studio**:
    - Selecciona la carpeta `FRONTED` como proyecto.
    - Espera la sincronización de Gradle.
3.  **Configurar API**:
    - Asegúrate de que el backend esté corriendo.
    - En `NetworkConfig.kt` (o similar), verifica la `BASE_URL`. Para emulador suele ser `http://10.0.2.2:5000/`. Para dispositivo físico, usa la IP de tu red local (ej. `http://192.168.1.XX:5000/`).
4.  **Ejecutar**:
    - Conecta tu dispositivo o inicia un emulador.
    - Dale al botón ▶️ "Run 'app'".

## 📱 Flujo de Uso (Escaneo)

1.  Iniciar sesión.
2.  Ir a la sección de **Exámenes** o usar el acceso directo de **Escanear**.
3.  Seleccionar el examen a calificar.
4.  Enfocar la cartilla con la cámara.
5.  Capturar la imagen (la app sugiere o permite recorte).
6.  Confirmar el envío.
7.  Verificar la nota y las respuestas marcadas en el overlay de resultado.
8.  Guardar la calificación.

## 🤝 Contribución

- Mantener el estilo de código Kotlin oficial.
- Usar componentes de Material 3.
- Seguir el patrón MVVM.
- Crear ramas por feature (`feature/scan-update`, `fix/login-bug`).
