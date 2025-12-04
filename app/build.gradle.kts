plugins {
    // 필수 플러그인
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // (선택 사항) Compose 컴파일러 관련 플러그인. buildFeatures에 설정이 있으므로 유지
    alias(libs.plugins.kotlin.compose)

    // Google Services (Firebase)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.ippo.taskflow"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ippo.taskflow"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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

    // JavaVersion.VERSION_11은 문자열로 처리되므로 11 버전으로 통일
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // Compose 설정
    buildFeatures {
        compose = true
    }

    // 컴파일러 버전 지정 (libs.versions.toml에서 가져오는 것을 권장)
    composeOptions {
        // kotlinCompilerExtensionVersion = "..." (버전을 여기서 지정하거나 libs.versions.toml에서 관리)
    }

    // 패키징 옵션 (Compose 프로젝트에서 일반적으로 추가)
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // -------------------------------------------------------------------------
    // 1. AndroidX Core & Base Libraries
    // -------------------------------------------------------------------------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // implementation(libs.androidx.appcompat) // Compose 프로젝트에서는 일반적으로 불필요함
    // implementation(libs.androidx.activity) // Compose Activity가 대체
    // implementation(libs.material) // Compose Material 3가 대체

    // -------------------------------------------------------------------------
    // 2. Compose Libraries
    // -------------------------------------------------------------------------
    // Compose BOM (버전 관리)
    implementation(platform(libs.androidx.compose.bom))

    // 필수 Compose 라이브러리
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation)

    // 추가 Compose 기능
    implementation("androidx.compose.material:material-icons-extended") // 확장 아이콘
    implementation(libs.androidx.navigation.compose) // 네비게이션

    // 디버깅 및 프리뷰 (debugImplementation으로 분류)
    implementation(libs.androidx.compose.ui.tooling.preview)


    // -------------------------------------------------------------------------
    // 3. Firebase Libraries
    // -------------------------------------------------------------------------
    // Firebase BOM (버전 관리)
    implementation(platform("com.google.firebase:firebase-bom:34.6.0")) // 최신 버전으로 업데이트를 고려하세요

    // 필요한 Firebase 모듈
    implementation("com.google.firebase:firebase-auth-ktx") // Kotlin KTX 버전 권장
    implementation("com.google.firebase:firebase-firestore-ktx") // Kotlin KTX 버전 권장

    // -------------------------------------------------------------------------
    // 4. Testing Dependencies
    // -------------------------------------------------------------------------
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Compose 테스트 (BOM으로 버전 관리)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // -------------------------------------------------------------------------
    // 5. Debugging Dependencies
    // -------------------------------------------------------------------------
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// 다음 의존성들은 제거했습니다:
// - libs.androidx.appcompat: Compose에서 일반적으로 불필요
// - libs.material: Compose Material 3가 대체
// - libs.androidx.activity: androidx.activity.compose가 대체
// - libs.androidx.constraintlayout: Compose 레이아웃(ConstraintLayout Compose)이 대체