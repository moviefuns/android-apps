// Top-level build file
allprojects {
    repositories {
        google()
//        jcenter() // 4.x 时代常用的经典仓库
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
