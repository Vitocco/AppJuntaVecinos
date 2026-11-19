plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // KSP (Kotlin Symbol Processing): genera código automáticamente al compilar.
    // Room lo necesita para crear la implementación real de los @Dao y la @Database.
    id("com.google.devtools.ksp") version "2.3.6"
}

android {
    namespace = "com.example.appjuntavecinos"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.appjuntavecinos"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // Permite usar java.time (LocalDate, YearMonth) en Android 7 (minSdk 24)
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
        // Genera BuildConfig.DEBUG: lo usamos para activar el log de red SOLO en debug
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.10.2")

    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3)
    // Room: base de datos local (SQLite) usando clases de Kotlin en vez de SQL a mano.
    // room-runtime → el núcleo: @Entity (tablas), @Dao (consultas) y @Database.
    implementation("androidx.room:room-runtime:2.8.3")
    // room-ktx → extras para Kotlin: funciones "suspend" (corrutinas) y Flow en los DAO,
    // así las consultas no congelan la pantalla y la UI se actualiza sola al cambiar los datos.
    implementation("androidx.room:room-ktx:2.8.3")
    // room-compiler → lee tus anotaciones y genera el código SQL. Va con "ksp" (no implementation)
    // porque solo se usa al compilar; no viaja dentro de la app.
    ksp("androidx.room:room-compiler:2.8.3")
    // Trae la función viewModel() para usar ViewModels dentro de un @Composable.
    // El ViewModel guarda el estado de la pantalla y sobrevive a rotaciones del celular.
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")

    // ---- Conexión con el backend (REST) ----
    // Retrofit: convierte las rutas de la API en funciones de Kotlin (ApiService)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // Gson: transforma JSON <-> data classes (DTOs)
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    // OkHttp: el cliente HTTP real; el interceptor agrega el token a cada petición
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Muestra en Logcat las peticiones (solo en debug y sin imprimir el token)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // DataStore: guarda la sesión (token, nombre, rol) aunque se cierre la app
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Recargar datos cada vez que una pantalla vuelve a verse (LifecycleResumeEffect)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")

    // Custom Tabs: abre el formulario de Webpay dentro de la app (Etapa 4)
    implementation("androidx.browser:browser:1.8.0")

    // java.time en versiones antiguas de Android
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // WorkManager → ejecuta la notificación aunque la app esté cerrada
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}