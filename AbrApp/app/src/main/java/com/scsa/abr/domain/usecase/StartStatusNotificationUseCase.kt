package com.scsa.abr.domain.usecase

import com.scsa.abr.domain.model.AbrProfile
import com.scsa.abr.domain.repository.BleGattRepository
import javax.inject.Inject

class StartStatusNotificationUseCase @Inject constructor(
    private val bleGattRepository: BleGattRepository
) {
    operator fun invoke() {
        bleGattRepository.startNotifications(
            AbrProfile.SERVICE_UUID,
            AbrProfile.CHARACTERISTIC_STATUS_UUID
        )
    }
}
