buildscript {
    repositories {
        google()
        maven("https://jitpack.io")
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.1.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    }
}

allprojects {
    repositories {
        google()
        maven("https://jitpack.io")
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}