package com.scsa.abr.di

import com.scsa.abr.data.AltBeaconRepositoryImpl
import com.scsa.abr.data.BleGattRepositoryImpl
import com.scsa.abr.data.BlePermissionRepositoryImpl
import com.scsa.abr.data.NavigationRepositoryImpl
import com.scsa.abr.domain.navigation.executor.FakeExecutor
import com.scsa.abr.domain.navigation.executor.NavigationExecutor
import com.scsa.abr.domain.navigation.executor.LowVoltageExecutor
import com.scsa.abr.domain.repository.BeaconRepository
import com.scsa.abr.domain.repository.BleGattRepository
import com.scsa.abr.domain.repository.BlePermissionRepository
import com.scsa.abr.domain.repository.NavigationRepository
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

    @Binds
    @Singleton
    abstract fun bindNavigationRepository(
        impl: NavigationRepositoryImpl
    ): NavigationRepository

    @Binds
    @Singleton
    abstract fun bindNavigationExecutor(
        impl: FakeExecutor
    ): NavigationExecutor
}