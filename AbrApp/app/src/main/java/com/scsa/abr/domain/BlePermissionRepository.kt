package com.scsa.abr.domain

interface BlePermissionRepository {
    fun getRequiredPermissions(): Array<String>
    fun hasAllPermissions(): Boolean
}