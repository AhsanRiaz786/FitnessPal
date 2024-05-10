plugins {
    alias(libs.plugins.androidApplication)
//    id("com.android.application")
    id("com.google.gms.google-services")


}

android {
    namespace = "com.example.fitnesspal"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fitnesspal"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))

    // Add the dependency for the Realtime Database library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-database")

    // https://mvnrepository.com/artifact/mysql/mysql-connector-java
    //noinspection UseTomlInstead
//    implementation("com.google.firebase:firebase-database")

    //noinspection GradlePath
    implementation(files("C:/Users/ahsan/OneDrive/Desktop/Google Maps Scraper/mysql-connector-java-8.0.28.jar"))
    implementation ("com.google.android.material:material:1.12.0")
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


}
