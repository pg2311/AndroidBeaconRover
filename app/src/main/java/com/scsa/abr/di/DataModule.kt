package com.scsa.abr.di

import com.scsa.abr.data.FakeBeaconRepositoryImpl
import com.scsa.abr.domain.BeaconRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {
    @Provides
    @Singleton
    fun provideBeaconRepository(): BeaconRepository {
        return FakeBeaconRepositoryImpl()
    }
}