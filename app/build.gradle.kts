plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "com.mahmoud.vpnpro"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.mahmoud.vpnpro"
        minSdk = 24; targetSdk = 35
        versionCode = 1; versionName = "1.0.0"
        // رابط JSON للسيرفرات (Supabase REST / Firebase Hosting). فارغ = بيانات تجريبية
        buildConfigField("String", "SERVERS_URL", "\"\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"\"")
    }
    buildFeatures { compose = true; buildConfig = true }
    buildTypes { release { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt")) } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
