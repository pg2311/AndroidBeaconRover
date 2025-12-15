/*
 * ble_manager.h
 * BLE connection manager for ESP32-C6
 * Handles Bluetooth Low Energy communication with Android app
 */

#ifndef BLE_MANAGER_H
#define BLE_MANAGER_H

#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include "command_interface.h"

// BLE UUIDs
#define SERVICE_UUID        "3fd350f5-1c0c-4d79-847c-91877824399e"
#define CONTROL_CHAR_UUID   "253a357f-39cb-4989-8bdf-f6b5ae8b7c65"
#define STATUS_CHAR_UUID    "b876fdc9-618d-4ea1-a83f-7e07cc89f963"

// BLE device name
#define BLE_DEVICE_NAME     "AndroidBeaconRover"

class BLEManager : public BLEServerCallbacks, public BLECharacteristicCallbacks {
public:
  BLEManager(CommandInterface* commands);

  void begin();
  void update();

  bool isConnected() const;
  void sendStatus(const String& status);

  // Set callback for when valid command is received
  void setCommandReceivedCallback(void (*callback)());

  // BLEServerCallbacks
  void onConnect(BLEServer* pServer) override;
  void onDisconnect(BLEServer* pServer) override;

  // BLECharacteristicCallbacks
  void onWrite(BLECharacteristic* pCharacteristic) override;

private:
  CommandInterface* commands;
  BLEServer* pServer;
  BLECharacteristic* pControlCharacteristic;
  BLECharacteristic* pStatusCharacteristic;

  bool deviceConnected;
  bool oldDeviceConnected;

  unsigned long lastStatusUpdate;
  const unsigned long STATUS_UPDATE_INTERVAL = 1000; // 1 second

  // Callback function pointer
  void (*commandReceivedCallback)();

  void processCommand(const String& cmd);
};

#endif // BLE_MANAGER_H
