package com.scsa.abr.di

import com.scsa.abr.data.AltBeaconRepositoryImpl
import com.scsa.abr.data.BleGattRepositoryImpl
import com.scsa.abr.data.BlePermissionRepositoryImpl
import com.scsa.abr.domain.BeaconRepository
import com.scsa.abr.domain.BleGattRepository
import com.scsa.abr.domain.BlePermissionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindBeaconRepository(
        impl: AltBeaconRepositoryImpl
//        impl: FakeBeaconRepositoryImpl
    ): BeaconRepository

    @Binds
    @Singleton
    abstract fun bindBlePermissionRepository(
        impl: BlePermissionRepositoryImpl
    ): BlePermissionRepository

    @Binds
    @Singleton
    abstract fun bindBleGattRepository(
        impl: BleGattRepositoryImpl
    ): BleGattRepository
}