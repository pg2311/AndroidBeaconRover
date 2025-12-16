package com.scsa.abr.domain.repository

interface BlePermissionRepository {
    fun getRequiredPermissions(): Array<String>
    fun hasAllPermissions(): Boolean
}