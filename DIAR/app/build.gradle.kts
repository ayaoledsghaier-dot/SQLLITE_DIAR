
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.diar"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.diar"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)

    // Google Maps
    implementation(libs.play.services.maps)

    // Google AdMob
    implementation(libs.play.services.ads)

    // Volley - Communication avec Backend
    implementation("com.android.volley:volley:1.2.1")

    // Gson - Conversion JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // OpenStreetMap
    implementation("org.osmdroid:osmdroid-android:6.1.20")

    // Glide - Chargement des images
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // SQLite local: disponible directement avec Android
    // Pas besoin d'ajouter une dépendance supplémentaire.

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // biblio tessercat pour lire l'attestation et extraire ses données
    implementation("com.rmtheis:tess-two:9.1.0")
}
