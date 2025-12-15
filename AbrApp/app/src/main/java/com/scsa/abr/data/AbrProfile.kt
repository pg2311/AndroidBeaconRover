package com.scsa.abr.data

import java.util.UUID

object AbrProfile {
    const val DEVICE_NAME = "AndroidBeaconRover"
    val SERVICE_UUID: UUID = UUID.fromString("3fd350f5-1c0c-4d79-847c-91877824399e")
    val CHARACTERISTIC_CONTROL_UUID: UUID = UUID.fromString("253a357f-39cb-4989-8bdf-f6b5ae8b7c65")
    val CHARACTERISTIC_STATUS_UUID: UUID = UUID.fromString("b876fdc9-618d-4ea1-a83f-7e07cc89f963")

}