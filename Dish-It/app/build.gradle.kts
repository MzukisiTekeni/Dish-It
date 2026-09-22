<<<<<<< HEAD
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    //id("com.android.application")
    id("com.google.gms.google-services")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
val spoonacularApiKey = localProperties.getProperty("SPOONACULAR_API_KEY") ?: ""

android {
    namespace = "com.dish_it.dish_it"
    compileSdk {
        version = release(37) {
=======
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.dish_it.dish_it"
    compileSdk {
        version = release(36) {
>>>>>>> main
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.dish_it.dish_it"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
<<<<<<< HEAD

        buildConfigField("String", "SPOONACULAR_API_KEY", "\"$spoonacularApiKey\"")
    }

    buildFeatures {
        buildConfig = true
=======
>>>>>>> main
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
<<<<<<< HEAD
    // Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
// Used across every screen: cards, chips, buttons
    implementation("com.google.android.material:material:1.12.0")
// Layout containers used throughout (ConstraintLayout root, NestedScrollView content,
// RecyclerView lists/carousels, DrawerLayout for the hamburger menu)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
// Wrapping chip rows (Dietary Preferences / Ingredients to avoid / Favourite Cuisines
// on My Profile) - plain LinearLayout doesn't wrap, Flexbox does.
    implementation("com.google.android.flexbox:flexbox:3.0.0")
// Image loading for recipe photos / avatar (HealthScoreActivity, adapters)
    implementation("com.github.bumptech.glide:glide:4.16.0")
// --- Uncomment when you wire up Spoonacular - see README's "Connecting
//     Spoonacular" section for how these get used. ---
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))



    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)


=======
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
>>>>>>> main
}