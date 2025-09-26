plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    alias(libs.plugins.google.gms.google.services)
    id("org.jetbrains.dokka")
    // (Firebase를 google-services.json으로 초기화할 때만 활성)
    // id("com.google.gms.google-services")
}

android {
    namespace = "com.todoc.todoc_ota_application"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.todoc.todoc_ota_application"
        minSdk = 28
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
}
// ✅ 모든 Dokka 태스크 공통: 우선 전부 suppress
tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    dokkaSourceSets.configureEach {
        suppress.set(true) // 전부 끄고
    }
}

// ✅ 우리가 쓸 기본 태스크(dokkaGfm)만 main 소스셋을 켜고 설정
tasks.named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaGfm") {
    outputDirectory.set(buildDir.resolve("dokka/gfm"))

    dokkaSourceSets.named("main") {
        suppress.set(false)              // main만 켬
        includeNonPublic.set(false)
        skipDeprecated.set(false)
        reportUndocumented.set(false)
        jdkVersion.set(8)

        // 소스 링크 (필요한 쪽만 남기세요)
        sourceLink {
            localDirectory.set(file("src/main/java"))
            remoteUrl.set(uri("https://github.com/<ORG>/<REPO>/tree/main/app/src/main/java").toURL())
            remoteLineSuffix.set("#L")
        }
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(uri("https://github.com/<ORG>/<REPO>/tree/main/app/src/main/kotlin").toURL())
            remoteLineSuffix.set("#L")
        }

        // 외부 문서 링크
        externalDocumentationLink {
            url.set(uri("https://developer.android.com/reference/").toURL())
        }
        externalDocumentationLink {
            url.set(uri("https://kotlinlang.org/api/kotlinx.coroutines/").toURL())
        }
    }
}
val copyDokkaToMintlify by tasks.registering(Copy::class) {
    from(project(":app").layout.buildDirectory.dir("dokka/gfm"))
    into(layout.projectDirectory.dir("docs/reference"))
    dependsOn(":app:dokkaGfm")
}
dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.appcheck.debug)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.firebase.appcheck.playintegrity)
    debugImplementation(libs.firebase.appcheck.debug)

    implementation("androidx.navigation:navigation-fragment-ktx:2.8.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ViewModel delegate 등 편의
    implementation("androidx.fragment:fragment-ktx:1.7.1")

    // 장시간 처리/재시작 복원 시 OTA에 사용
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // ===== Firebase Storage (OTA 파일/Range 전송용) =====
//    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
//    implementation("com.google.firebase:firebase-storage-ktx")
//    implementation("com.google.firebase:firebase-appcheck-playintegrity")
}