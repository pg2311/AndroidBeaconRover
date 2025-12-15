/*
 * ble_manager.cpp
 * BLE connection manager implementation
 */

#include "ble_manager.h"

BLEManager::BLEManager(CommandInterface* commands)
  : commands(commands),
    pServer(nullptr),
    pControlCharacteristic(nullptr),
    pStatusCharacteristic(nullptr),
    deviceConnected(false),
    oldDeviceConnected(false),
    lastStatusUpdate(0),
    commandReceivedCallback(nullptr) {
}

void BLEManager::begin() {
  Serial.println("[BLE] Initializing...");

  // Initialize BLE Device
  BLEDevice::init(BLE_DEVICE_NAME);

  // Create BLE Server
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(this);

  // Create BLE Service
  BLEService* pService = pServer->createService(SERVICE_UUID);

  // Create Control Characteristic (Write)
  // This receives commands from the Android app
  pControlCharacteristic = pService->createCharacteristic(
    CONTROL_CHAR_UUID,
    BLECharacteristic::PROPERTY_WRITE |
    BLECharacteristic::PROPERTY_WRITE_NR
  );
  pControlCharacteristic->setCallbacks(this);

  // Create Status Characteristic (Read + Notify)
  // This sends status updates to the Android app
  pStatusCharacteristic = pService->createCharacteristic(
    STATUS_CHAR_UUID,
    BLECharacteristic::PROPERTY_READ |
    BLECharacteristic::PROPERTY_NOTIFY
  );
  pStatusCharacteristic->addDescriptor(new BLE2902());

  // Start the service
  pService->start();

  // Start advertising
  BLEAdvertising* pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);
  pAdvertising->setMaxPreferred(0x12);
  BLEDevice::startAdvertising();

  Serial.println("[BLE] Service started");
  Serial.print("[BLE] Device name: ");
  Serial.println(BLE_DEVICE_NAME);
  Serial.println("[BLE] Waiting for connection...");
}

void BLEManager::update() {
  // Handle connection state changes
  if (!deviceConnected && oldDeviceConnected) {
    // Disconnected - restart advertising
    delay(500);
    pServer->startAdvertising();
    Serial.println("[BLE] Disconnected. Advertising restarted.");
    oldDeviceConnected = deviceConnected;
  }

  if (deviceConnected && !oldDeviceConnected) {
    // Connected
    oldDeviceConnected = deviceConnected;
    Serial.println("[BLE] Connected!");
  }

  // Send periodic status updates
  if (deviceConnected) {
    unsigned long now = millis();
    if (now - lastStatusUpdate > STATUS_UPDATE_INTERVAL) {
      String status = commands->getStatus();
      sendStatus(status);
      lastStatusUpdate = now;
    }
  }
}

bool BLEManager::isConnected() const {
  return deviceConnected;
}

void BLEManager::setCommandReceivedCallback(void (*callback)()) {
  commandReceivedCallback = callback;
}

void BLEManager::sendStatus(const String& status) {
  if (deviceConnected && pStatusCharacteristic) {
    pStatusCharacteristic->setValue(status);
    pStatusCharacteristic->notify();
  }
}

// BLE Server Callbacks
void BLEManager::onConnect(BLEServer* pServer) {
  deviceConnected = true;
  Serial.println("[BLE] Client connected");
}

void BLEManager::onDisconnect(BLEServer* pServer) {
  deviceConnected = false;
  Serial.println("[BLE] Client disconnected");
}

// BLE Characteristic Callbacks
void BLEManager::onWrite(BLECharacteristic* pCharacteristic) {
  // Called when Android app writes to control characteristic
  Serial.println("[BLE] onWrite");
  if (pCharacteristic == pControlCharacteristic) {
    String value = pCharacteristic->getValue();

    // Validate command
    if (value.length() > 0 && value.length() <= 64) {
      // Valid command received - notify callback BEFORE processing
      Serial.println("[BLE] onWrite->validateCommand");
      if (commandReceivedCallback != nullptr) {
        commandReceivedCallback();
      }

      // Process the command
      processCommand(value);
    } else if (value.length() > 64) {
      Serial.printf("[BLE] ERROR: Command too long (%d bytes, max 64)\n", value.length());
    }
  }
}

void BLEManager::processCommand(const String& cmd) {
  // Echo command for debugging
  Serial.print("[BLE] Received: ");
  Serial.println(cmd);

  // Process through command interface
  commands->process(cmd);
}
