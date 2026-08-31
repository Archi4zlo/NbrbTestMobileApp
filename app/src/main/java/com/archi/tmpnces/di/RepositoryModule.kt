package com.archi.tmpnces.di

import com.archi.tmpnces.data.repository.RateRepositoryImpl
import com.archi.tmpnces.domain.repository.RateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
	
	@Binds
	@Singleton
	abstract fun bindRateRepository(impl: RateRepositoryImpl): RateRepository
}