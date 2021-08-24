// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlin_version : String by extra("1.5.0")

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
//        jcenter() // Warning: this repository is going to shut down soon
    }
}


tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}
