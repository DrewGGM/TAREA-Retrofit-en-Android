# Proyecto Demo: Retrofit en Android con Kotlin

Proyecto de demostración del uso de Retrofit para consumir APIs REST en Android, utilizando Jetpack Compose y la PokeAPI.

## 📋 Descripción

Este es un proyecto simple en Android que demuestra cómo implementar Retrofit para realizar peticiones HTTP a una API REST (PokeAPI). El proyecto está diseñado para ser **simple**, enfocándose en mostrar las capacidades de Retrofit sin agregar complejidad innecesaria.

## 🎯 Características Principales

- ✅ **Retrofit 2**: Consumo de API REST (PokeAPI)
- ✅ **Kotlin Coroutines**: Manejo de operaciones asíncronas
- ✅ **Jetpack Compose**: UI moderna y declarativa
- ✅ **Material 3**: Diseño siguiendo Material Design 3
- ✅ **MVVM Pattern**: Arquitectura Model-View-ViewModel
- ✅ **Repository Pattern**: Separación de lógica de datos
- ✅ **StateFlow**: Manejo reactivo de estados
- ✅ **Coil**: Carga de imágenes desde URLs
- ✅ **Testing**: Pruebas unitarias con MockWebServer y Strikt

## 🧪 Pruebas

El proyecto incluye 3 archivos de prueba que demuestran diferentes aspectos del testing con Retrofit:

### 1. IntegrationTest.kt
- Pruebas de integración completas
- Uso de MockWebServer para simular el servidor
- Verificación de serialización/deserialización con Gson
- Uso de Strikt para assertions

### 2. PokeApiServiceTest.kt
- Testing directo del servicio de Retrofit
- Simulación de respuestas HTTP exitosas y erróneas
- Validación de modelos de datos

### 3. PokemonRepositoryTest.kt
- Testing del patrón Repository
- Uso de MockK para simular dependencias
- Testing de flujos (Flow) con coroutines

## 🌐 API Utilizada

**PokeAPI**: https://pokeapi.co/

API gratuita y pública con información de Pokémon. No requiere autenticación.

Endpoints usados:
- `GET /pokemon?limit=20&offset=0` - Lista de Pokémon
- `GET /pokemon/{name}` - Detalles de un Pokémon específico

## 👨‍💻 Autores

- Andrew Garcia Mosquera
- Valeria Alarcon Munera
- Juan Camilo Soto
