#include "motor_control.h"
#include "command_interface.h"
#include "ble_manager.h"

// Create instances
MotorControl motors;
CommandInterface commands(&motors);
BLEManager bleManager(&commands);

// Safety timeout - stop motors if no command received
#define COMMAND_TIMEOUT_MS  10000
unsigned long lastCommandTime = 0;
bool timeoutEnabled = true;

// Callback function to update command timestamp
void onCommandReceived() {
  lastCommandTime = millis();
}

void setup() {
  Serial.begin(9600);
  delay(1000);

  Serial.println();
  Serial.println("[MAIN] ESP32-C6 Car Controller");
  Serial.println("[MAIN] (BLE + Serial Control Mode)");
  Serial.println();

  // Initialize motor control
  motors.begin();

  // Initialize command interface
  commands.begin();

  // Initialize BLE
  bleManager.begin();

  // Register callback for BLE commands
  bleManager.setCommandReceivedCallback(onCommandReceived);

  Serial.println();
  Serial.println("System ready!");
  Serial.println("Type 'help' for command list");
  Serial.println("BLE device ready for Android connection");
  Serial.println();
}

void loop() {
  // Update BLE connection state
  bleManager.update();
  commands.update();

  // Safety timeout - stop if no commands received
  if (timeoutEnabled && motors.isMoving()) {
    if (millis() - lastCommandTime > COMMAND_TIMEOUT_MS) {
      Serial.println("[Safety] Command timeout - stopping motors");
      motors.stop();
    }
  }

  // Read commands from Serial
  if (Serial.available()) {
    String input = Serial.readStringUntil('\n');
    input.trim();

    if (input.length() > 0) {
      lastCommandTime = millis();

      // Echo input
      Serial.print("> ");
      Serial.println(input);

      // Handle special commands
      if (input.equalsIgnoreCase("timeout off")) {
        timeoutEnabled = false;
        Serial.println("[Config] Timeout disabled");
      }
      else if (input.equalsIgnoreCase("timeout on")) {
        timeoutEnabled = true;
        Serial.println("[Config] Timeout enabled");
      }
      else {
        // Process motor command
        commands.process(input);
      }
    }
  }

  // Small delay to prevent watchdog issues
  delay(10);
}
