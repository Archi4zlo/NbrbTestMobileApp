plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.ksp)
	alias(libs.plugins.hilt)
}

android {
	namespace = "com.archi.tmpnces"
	compileSdk {
		version = release(37) {
			minorApiLevel = 1
		}
	}
	
	defaultConfig {
		applicationId = "com.archi.tmpnces"
		minSdk = 28
		targetSdk = 36
		versionCode = 1
		versionName = "1.0"
		
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}
	
	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	buildFeatures {
		compose = true
		buildConfig = true
	}
}


dependencies {
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.graphics)
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.compose.material3)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.compose.ui.test.junit4)
	debugImplementation(libs.androidx.compose.ui.tooling)
	debugImplementation(libs.androidx.compose.ui.test.manifest)
	
	// Hilt
	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)

// Decompose
	implementation(libs.decompose)
	implementation(libs.decompose.extensions.compose)

// MVIKotlin
	implementation(libs.mvikotlin)
	implementation(libs.mvikotlin.main)
	implementation(libs.mvikotlin.coroutines)

// Room
	implementation(libs.room.runtime)
	implementation(libs.room.ktx)
	implementation(libs.room.paging)
	ksp(libs.room.compiler)

// Paging
	implementation(libs.paging.runtime)
	implementation(libs.paging.compose)

// Coroutines
	implementation(libs.kotlinx.coroutines.android)

// Serialization
	implementation(libs.kotlinx.serialization.json)

// Lifecycle
	implementation(libs.androidx.lifecycle.runtime.compose)

// Timber
	implementation(libs.timber)


	// Retrofit
	implementation(libs.retrofit)
	implementation(libs.retrofit.kotlinx.converter)
	implementation(libs.okhttp)
	implementation(libs.okhttp.logging)

	// Testing
	testImplementation(libs.kotlinx.coroutines.test)
	testImplementation(libs.room.testing)
	testImplementation(libs.paging.testing)
	testImplementation(libs.turbine)
	testImplementation(libs.mockk)
}