# YardSale - Aplicación Android

Una aplicación Android desarrollada con Jetpack Compose y Firebase para gestionar yard sales (ventas de garaje).

## Características Principales

### 🔐 Autenticación y Usuarios
- **Sistema de usuarios**: Vendedores, compradores, invitados, administradores y superadmin
- **Registro e inicio de sesión**: Con validación en tiempo real y mensajes de error multidioma
- **Sesiones de invitados**: Sistema de límite de sesiones para usuarios no registrados
- **Autenticación Firebase**: Integración completa con Firebase Authentication

### 🗺️ Mapa en Tiempo Real
- **Google Maps integrado**: Mapa real con la ubicación actual del usuario
- **Ubicación en tiempo real**: Obtiene y muestra la ubicación GPS del dispositivo
- **Marcadores de yard sales**: Muestra las yard sales activas en el mapa
- **Permisos de ubicación**: Manejo automático de permisos de ubicación
- **Interfaz intuitiva**: Controles de zoom, botón de mi ubicación y marcadores interactivos

### 🌍 Multidioma
- **Soporte completo**: Español (por defecto), inglés y francés
- **Interfaz localizada**: Todos los textos y mensajes de error están internacionalizados
- **Detecta idioma del dispositivo**: Se adapta automáticamente al idioma del usuario

### 🎨 Interfaz de Usuario
- **Material Design 3**: Diseño moderno y accesible
- **Menús flotantes**: Para usuarios registrados e invitados
- **Validación visual**: Campos de entrada con iconos y estados de error
- **Experiencia fluida**: Navegación intuitiva y feedback visual

## Tecnologías Utilizadas

- **Jetpack Compose**: UI declarativa moderna
- **Firebase**: Authentication, Firestore, y servicios en la nube
- **Google Maps**: Mapas y servicios de ubicación
- **Kotlin Coroutines**: Programación asíncrona
- **MVVM Architecture**: ViewModel y StateFlow para gestión de estado
- **Material Design 3**: Sistema de diseño moderno

## Configuración del Proyecto

### Prerrequisitos
- Android Studio Hedgehog o superior
- JDK 17 o superior
- Dispositivo Android con Google Play Services

### Dependencias Principales
```kotlin
// Google Maps y ubicación
implementation("com.google.android.gms:play-services-maps:18.2.0")
implementation("com.google.android.gms:play-services-location:21.2.0")
implementation("com.google.maps.android:maps-compose:4.3.0")

// Firebase
implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-firestore")

// Jetpack Compose
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-extended")
```

### Configuración de Firebase
1. Crear proyecto en Firebase Console
2. Descargar `google-services.json` y colocarlo en `app/`
3. Habilitar Authentication y Firestore en Firebase Console
4. Configurar reglas de seguridad en Firestore

### Configuración de Google Maps
1. Obtener API Key de Google Cloud Console
2. Habilitar Maps SDK for Android
3. Configurar la API Key en `AndroidManifest.xml`

## Funcionalidades del Mapa

### Ubicación en Tiempo Real
- **GPS de alta precisión**: Utiliza `FusedLocationProviderClient` para obtener ubicación precisa
- **Manejo de permisos**: Solicita automáticamente permisos de ubicación
- **Estados de carga**: Muestra indicadores de progreso mientras obtiene la ubicación
- **Manejo de errores**: Interfaz para reintentar cuando hay problemas de ubicación

### Marcadores de Yard Sales
- **Ubicaciones dinámicas**: Los marcadores se generan basados en las yard sales activas
- **Información detallada**: Título, dirección y rating al tocar los marcadores
- **Clustering futuro**: Preparado para agrupar marcadores cercanos

### Controles del Mapa
- **Zoom automático**: Se centra automáticamente en la ubicación del usuario
- **Botón de mi ubicación**: Permite al usuario volver a su ubicación
- **Controles de zoom**: Botones + y - para control manual del zoom
- **Interfaz limpia**: Sin barra de herramientas para una experiencia más limpia

## Estructura del Proyecto

```
app/src/main/java/com/internetfacilito/yardsale/
├── data/
│   ├── model/          # Modelos de datos
│   ├── repository/     # Repositorio Firebase
│   └── util/          # Utilidades
├── ui/
│   ├── screens/       # Pantallas de la aplicación
│   ├── theme/         # Temas y estilos
│   └── viewmodel/     # ViewModels
└── MainActivity.kt    # Actividad principal
```

## Estados de la Aplicación

- **Loading**: Cargando datos iniciales
- **Authenticated**: Usuario autenticado
- **Guest**: Usuario invitado
- **GuestLimitReached**: Límite de sesiones de invitado alcanzado
- **Success**: Operación exitosa
- **Error**: Error con mensaje específico

## Permisos Requeridos

- `INTERNET`: Para comunicación con Firebase
- `ACCESS_NETWORK_STATE`: Para verificar conectividad
- `ACCESS_FINE_LOCATION`: Para ubicación precisa
- `ACCESS_COARSE_LOCATION`: Para ubicación aproximada

## Próximas Funcionalidades

- [ ] Crear yard sales desde la aplicación
- [ ] Filtros y búsqueda en el mapa
- [ ] Notificaciones push
- [ ] Sistema de ratings y comentarios
- [ ] Chat entre usuarios
- [ ] Fotos de yard sales
- [ ] Rutas y navegación

## Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles. 