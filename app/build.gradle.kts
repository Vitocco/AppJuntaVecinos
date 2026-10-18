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
    }
    buildFeatures {
        compose = true
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
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}