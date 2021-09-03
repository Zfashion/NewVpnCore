import com.android.build.gradle.api.LibraryVariant

plugins {
    id("com.android.library")
    kotlin("android")
}

val usePreBuildLibs: Boolean = true

android {
    compileSdkVersion(30)
    buildToolsVersion = "30.0.3"

    defaultConfig {
        minSdkVersion(19)
        targetSdkVersion(30)
        versionCode(1)
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    if (!usePreBuildLibs) {
        externalNativeBuild {
            cmake {
                path = File("${projectDir}/src/main/cpp/CMakeLists.txt")
            }
        }
    } else {
        sourceSets {
            getByName("main") {
                jniLibs {
                    srcDirs("${projectDir}/preBuildLibs")
                }
            }
        }
    }

    sourceSets {
        getByName("main") {
            res {
                srcDirs("src/main/res", "src/ui/src")
            }
            java {
                srcDirs("src/main/java", "src/ui/java")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

//针对处理OpenVPNThreadv3的任务
var swigcmd = "swig"
// Workaround for Mac OS X since it otherwise does not find swig and I cannot get
// the Exec task to respect the PATH environment :(
if (File("/usr/local/bin/swig").exists())
    swigcmd = "/usr/local/bin/swig"


fun registerGenTask(variantName: String, variantDirName: String): File {
    val baseDir = File(buildDir, "generated/source/ovpn3swig/${variantDirName}")
    val genDir = File(baseDir, "net/openvpn/ovpn3")

    tasks.register<Exec>("generateOpenVPN3Swig${variantName}")
    {

        doFirst {
            mkdir(genDir)
        }
        commandLine(listOf(swigcmd, "-outdir", genDir, "-outcurrentdir", "-c++", "-java", "-package", "net.openvpn.ovpn3",
            "-Isrc/main/cpp/openvpn3/client", "-Isrc/main/cpp/openvpn3/",
            "-o", "${genDir}/ovpncli_wrap.cxx", "-oh", "${genDir}/ovpncli_wrap.h",
            "src/main/cpp/openvpn3/javacli/ovpncli.i"))

    }
    return baseDir
}


android.libraryVariants.all(object : Action<LibraryVariant> {
    override fun execute(t: LibraryVariant) {
        val sourceDir = registerGenTask(t.name, t.baseName.replace("-", "/"))
        val task = tasks.named("generateOpenVPN3Swig${t.name}").get()

        t.registerJavaGeneratingTask(task, sourceDir)
    }
})

dependencies {

    implementation("androidx.annotation:annotation:1.2.0")
    implementation(project(mapOf("path" to ":unitevpn")))

    val kotlinVersion = rootProject.extra.get("kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:1.4.0")
    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    annotationProcessor("com.google.auto.service:auto-service:1.0")
    implementation("com.google.auto.service:auto-service-annotations:1.0")
}



