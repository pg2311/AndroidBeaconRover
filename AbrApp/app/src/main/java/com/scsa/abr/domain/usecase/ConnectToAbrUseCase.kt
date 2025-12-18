package com.scsa.abr.domain.usecase

import com.scsa.abr.domain.model.AbrProfile
import com.scsa.abr.domain.repository.BleGattRepository
import javax.inject.Inject

class ConnectToAbrUseCase @Inject constructor(
    private val bleGattRepository: BleGattRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return bleGattRepository.connect(AbrProfile.DEVICE_ADDRESS)
    }
}
