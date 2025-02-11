plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.apps.rescueconnect"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.apps.rescueconnect"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "4.0"

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
        buildConfig = true
    }
}

dependencies {
    implementation ("androidx.appcompat:appcompat:1.4.1")
    implementation ("com.google.android.material:material:1.10.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.4.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    implementation ("androidx.navigation:navigation-fragment:2.4.1")
    implementation ("androidx.navigation:navigation-ui:2.4.1")
    implementation ("androidx.legacy:legacy-support-v4:1.0.0")
    implementation ("com.google.firebase:firebase-database:20.0.0")
    implementation ("com.google.firebase:firebase-storage:20.0.0")
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.3")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.4.0")

    // Rx Binding
    implementation ("com.jakewharton.rxbinding:rxbinding:1.0.1")
    // Card View
    implementation ("androidx.cardview:cardview:1.0.0")
    //Recycler View
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    // Gson Dependency
    implementation ("com.google.code.gson:gson:2.8.7")
    //Custom Toast
    implementation ("com.github.NaimishTrivedi:FBToast:1.0")
    // Circular Image View
    implementation ("de.hdodenhof:circleimageview:3.0.1")
    // Pi Charts
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    // Bottom Navigation Bar
    implementation ("it.sephiroth.android.library.bottomnavigation:bottom-navigation:3.0.0")
    // Crop Image
    implementation ("com.theartofdev.edmodo:android-image-cropper:2.8.0")
    implementation ("com.squareup.picasso:picasso:2.5.2")
    // Glid
    implementation ("com.github.bumptech.glide:glide:4.12.0")

    implementation ("com.google.android.libraries.places:places:2.6.0")
    implementation ("com.google.android.gms:play-services-maps:17.0.0")
    implementation ("com.google.android.gms:play-services-location:17.0.0")
    implementation ("com.google.android.gms:play-services-identity:17.0.0")
    implementation ("com.google.android.gms:play-services-places:17.0.0")
    implementation ("com.google.maps.android:android-maps-utils:0.5")

    // Image Slider
    implementation ("com.github.denzcoskun:ImageSlideshow:0.1.0")

    // Location Serach Bar
    implementation ("com.github.mancj:MaterialSearchBar:0.8.1")
}