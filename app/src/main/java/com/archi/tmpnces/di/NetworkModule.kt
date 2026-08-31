package com.archi.tmpnces.di

import com.archi.tmpnces.BuildConfig
import com.archi.tmpnces.data.remote.NbrbApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
	
	private const val BASE_URL = "https://api.nbrb.by/"
	
	@Provides
	@Singleton
	fun provideJson(): Json = Json {
		ignoreUnknownKeys = true
	}
	
	@Provides
	@Singleton
	fun provideOkHttpClient(): OkHttpClient {
		val loggingInterceptor = HttpLoggingInterceptor().apply {
			level = if (BuildConfig.DEBUG) {
				HttpLoggingInterceptor.Level.BODY
			} else {
				HttpLoggingInterceptor.Level.NONE
			}
		}
		return OkHttpClient.Builder()
			.addInterceptor(loggingInterceptor)
			.build()
	}
	
	@Provides
	@Singleton
	fun provideRetrofit(
		okHttpClient: OkHttpClient, json: Json
	): Retrofit = Retrofit.Builder()
		.baseUrl(BASE_URL)
		.client(okHttpClient)
		.addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
		.build()
	
	@Provides
	@Singleton
	fun provideNbrbApiService(retrofit: Retrofit): NbrbApiService = retrofit.create(NbrbApiService::class.java)
}