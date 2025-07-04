plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.androidx.room)
}

android {
    namespace = "com.nawala.keuangan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nawala.keuangan"
        minSdk = 21
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/native-image/reflect-config.json"
            excludes += "META-INF/native-image/resource-config.json"
        }
    }
}
room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.itext.kernel)
    implementation(libs.itext.layout)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
}