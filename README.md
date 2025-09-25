# AIAPAEC - Aplicación Móvil Android

## 📱 Descripción del Proyecto

AIAPAEC es una aplicación móvil nativa para Android desarrollada en Kotlin utilizando Jetpack Compose. La aplicación está diseñada para gestionar exámenes, resultados y actividades académicas con una interfaz moderna y profesional.

## 🏗️ Arquitectura

### Patrón MVVM (Model-View-ViewModel)
- **Model**: Manejo de datos y lógica de negocio
- **View**: Interfaces de usuario con Jetpack Compose
- **ViewModel**: Gestión de estado y lógica de presentación

### Estructura del Proyecto
```
app/src/main/java/com/jotadev/aiapaec/
├── MainActivity.kt                 # Actividad principal
├── navigation/                     # Sistema de navegación
│   ├── AppNavigation.kt           # Configuración de navegación principal
│   ├── BottomNavItem.kt           # Items de navegación inferior
│   ├── BottomNavigationBar.kt     # Barra de navegación inferior
│   └── NavigationRoutes.kt        # Definición de rutas
└── ui/
    ├── components/                # Componentes reutilizables
    │   └── TopBar.kt             # Barras superiores personalizadas
    ├── screens/                   # Pantallas de la aplicación
    │   ├── exams/                # Módulo de exámenes
    │   ├── home/                 # Pantalla principal
    │   ├── login/                # Autenticación
    │   ├── main/                 # Pantalla principal con navegación
    │   ├── results/              # Resultados de exámenes
    │   └── settings/             # Configuraciones
    └── theme/                    # Sistema de diseño
        ├── Color.kt              # Paleta de colores
        ├── Font.kt               # Tipografías
        ├── Theme.kt              # Tema principal
        └── Type.kt               # Estilos de texto
```

## 🎨 Sistema de Diseño

### Colores Corporativos AIAPAEC
- **Primario**: Crimson (Rojo corporativo)
- **Secundario**: Gold (Dorado)
- **Superficie**: Blanco y grises
- **Texto**: Blanco sobre fondos oscuros, negro sobre fondos claros

### Tipografía
- Fuente personalizada integrada con Google Fonts
- Jerarquía tipográfica consistente
- Estilos optimizados para legibilidad

## 📱 Funcionalidades Principales

### 🔐 Autenticación
- Pantalla de login con validación de campos
- Gestión de estado de autenticación
- Navegación automática tras login exitoso

### 🏠 Pantalla Principal (Home)
- Dashboard con acciones rápidas
- Información del usuario
- Navegación a módulos principales

### 📝 Módulo de Exámenes
- Gestión de exámenes
- Interfaz preparada para funcionalidades futuras

### 📊 Resultados
- Visualización de resultados de exámenes
- Interfaz preparada para análisis de datos

### ⚙️ Configuraciones
- Ajustes de la aplicación
- Preferencias del usuario

## 🛠️ Tecnologías y Dependencias

### Tecnologías Principales
- **Kotlin**: 2.2.20
- **Android Gradle Plugin**: 8.13.0
- **Jetpack Compose**: BOM 2025.09.01
- **Material Design 3**: 1.4.0

### Dependencias Clave
```kotlin
// Core Android
implementation("androidx.core:core-ktx:1.17.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
implementation("androidx.activity:activity-compose:1.11.0")

// Jetpack Compose
implementation(platform("androidx.compose:compose-bom:2025.09.01"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.ui:ui-graphics")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.compose.material3:material3")

// Navegación
implementation("androidx.navigation:navigation-compose:2.9.5")

// UI/UX
implementation("androidx.compose.material:material-icons-extended:1.7.8")
implementation("androidx.compose.ui:ui-text-google-fonts:1.9.2")
implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")
```

## 🔧 Configuración del Proyecto

### Requisitos del Sistema
- **Android Studio**: Última versión estable
- **SDK mínimo**: API 25 (Android 7.1)
- **SDK objetivo**: API 36
- **Java**: Versión 11

### Configuración de Compilación
```kotlin
android {
    compileSdk = 36
    
    defaultConfig {
        applicationId = "com.jotadev.aiapaec"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
}
```

## 🚀 Instalación y Ejecución

### Clonar el Repositorio
```bash
git clone [URL_DEL_REPOSITORIO]
cd FRONTED
```

### Compilar el Proyecto
```bash
# En Windows
./gradlew build

# En Linux/Mac
./gradlew build
```

### Ejecutar en Dispositivo/Emulador
1. Conectar dispositivo Android o iniciar emulador
2. Ejecutar desde Android Studio o usar:
```bash
./gradlew installDebug
```

## 📋 Características Técnicas

### Gestión de Estado
- **StateFlow** para manejo reactivo de estado
- **ViewModel** para persistencia durante cambios de configuración
- **Compose State** para estado local de UI

### Navegación
- **Navigation Compose** para navegación declarativa
- Rutas tipadas y navegación segura
- Gestión de back stack automática

### Interfaz de Usuario
- **Material Design 3** como sistema de diseño
- **Jetpack Compose** para UI declarativa
- Componentes reutilizables y modulares
- Soporte para temas claro/oscuro

### Arquitectura de Pantallas
Cada pantalla sigue el patrón:
- **Screen.kt**: Composable de la interfaz
- **ViewModel.kt**: Lógica de negocio y estado
- **UiState**: Data class para estado de la pantalla

## 🎯 Buenas Prácticas Implementadas

### Código
- Separación clara de responsabilidades
- Funciones pequeñas y enfocadas
- Comentarios concisos en una línea
- Código profesional y escalable

### UI/UX
- Diseño consistente entre pantallas
- Navegación intuitiva
- Feedback visual apropiado
- Accesibilidad considerada

### Arquitectura
- Patrón MVVM bien definido
- Inyección de dependencias preparada
- Modularización por características
- Testabilidad mejorada

## 🔮 Funcionalidades Futuras

### Próximas Implementaciones
- [ ] Integración con API backend
- [ ] Autenticación con JWT
- [ ] Escaneo de códigos QR
- [ ] Análisis de resultados avanzado
- [ ] Notificaciones push
- [ ] Modo offline
- [ ] Exportación de datos

### Mejoras Técnicas
- [ ] Testing unitario y de integración
- [ ] CI/CD pipeline
- [ ] Optimización de rendimiento
- [ ] Internacionalización (i18n)
- [ ] Análisis de crashes

## 👥 Equipo de Desarrollo

**Desarrollador Principal**: JotaDev  
**Organización**: AIAPAEC  
**Versión**: 1.0  

## 📄 Licencia

Este proyecto es propiedad de AIAPAEC. Todos los derechos reservados.

---

**Nota**: Esta aplicación está en desarrollo activo. Las funcionalidades pueden cambiar en futuras versiones.