import java.time.Duration

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.tcc_final"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tcc_final"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    // Retrofit para comunicação HTTP
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Conversor Gson para transformar a classe Usuario em JSON
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Conversor Scalars para receber as mensagens de texto plano (String) da sua API
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
}




// Recria o tunel local em cada Run/build de debug, mesmo com o APK atualizado.
val adbLocal = androidComponents.sdkComponents.adb
val scriptApiLocal = rootProject.layout.projectDirectory.file("conectar-api.ps1").asFile.absolutePath
val conectarApiDebug by tasks.registering(Exec::class) {
    group = "development"
    description = "Conecta a API local aos dispositivos ADB na porta 8080."
    onlyIf { System.getProperty("os.name").startsWith("Windows", ignoreCase = true) }
    isIgnoreExitValue = true
    timeout.set(Duration.ofSeconds(45))
    doFirst {
        commandLine(
            "powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass",
            "-File", scriptApiLocal, "-AdbPath", adbLocal.get().asFile.absolutePath,
            "-EsperaSegundos", "15", "-DuranteBuild"
        )
    }
    doLast {
        if (executionResult.get().exitValue != 0) {
            logger.warn("[API local] Nao foi possivel configurar a conexao. Confira conectar-api.ps1 e execute Run novamente.")
        }
    }
}

tasks.matching { it.name == "assembleDebug" }.configureEach {
    finalizedBy(conectarApiDebug)
}
