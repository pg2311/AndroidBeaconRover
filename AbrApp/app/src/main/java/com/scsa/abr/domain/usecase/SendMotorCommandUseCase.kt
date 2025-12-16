package com.scsa.abr.domain.usecase

import com.scsa.abr.domain.model.AbrProfile
import com.scsa.abr.domain.repository.BleGattRepository
import javax.inject.Inject

class SendMotorCommandUseCase @Inject constructor (
    private val bleGattRepository: BleGattRepository
) {
    suspend operator fun invoke(command: String): Result<Unit> {
        val data = command.toByteArray(Charsets.UTF_8)
        return bleGattRepository.writeCharacteristic(
            serviceUuid = AbrProfile.SERVICE_UUID,
            charUuid = AbrProfile.CHARACTERISTIC_CONTROL_UUID,
            data
        )
    }
}