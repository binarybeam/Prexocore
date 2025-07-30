plugins {
    id("com.android.library") version "8.4.2"
    kotlin("android") version "1.9.10"
}

android {
    namespace = "com.prexoft.prexocore"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        targetSdk = 36
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

}
