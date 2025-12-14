/*
 * esp32c6_car.ino
 * ESP32-C6 Car Controller (Serial only, no BLE)
 *
 * Hardware:
 *   ESP32-C6 + L298N Motor Driver
 *   ENA - GPIO2, IN1 - GPIO3, IN2 - GPIO15
 *   IN3 - GPIO4, IN4 - GPIO5, ENB - GPIO6
 *
 * Commands (via Serial Monitor at 115200 baud):
 *   F / F:speed  - Forward
 *   B / B:speed  - Backward
 *   L / L:speed  - Turn left
 *   R / R:speed  - Turn right
 *   G / G:speed  - Rotate left (spin)
 *   H / H:speed  - Rotate right (spin)
 *   S            - Stop
 *   J:x:y        - Joystick (-100 to 100)
 *   M:left:right - Manual motor control (signed)
 *   V:speed      - Set default speed
 *   ?            - Query status
 */

#include "motor_control.h"
#include "command_interface.h"

// Create instances
MotorControl motors;
CommandInterface commands(&motors);

// Safety timeout - stop motors if no command received
#define COMMAND_TIMEOUT_MS  1000
unsigned long lastCommandTime = 0;
bool timeoutEnabled = true;

void setup() {
  Serial.begin(115200);
  delay(1000);

  Serial.println();
  Serial.println("================================");
  Serial.println("  ESP32-C6 Car Controller");
  Serial.println("  (Serial Control Mode)");
  Serial.println("================================");
  Serial.println();

  // Initialize motor control
  motors.begin();

  // Initialize command interface
  commands.begin();

  Serial.println();
  Serial.println("System ready!");
  Serial.println("Type 'help' for command list");
  Serial.println();
}

void loop() {
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
      if (input.equalsIgnoreCase("help")) {
        printHelp();
      }
      else if (input.equalsIgnoreCase("timeout off")) {
        timeoutEnabled = false;
        Serial.println("[Config] Timeout disabled");
      }
      else if (input.equalsIgnoreCase("timeout on")) {
        timeoutEnabled = true;
        Serial.println("[Config] Timeout enabled");
      }
      else if (input.equalsIgnoreCase("test")) {
        runTest();
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

void runTest() {
  Serial.println();
  Serial.println("=== Running Motor Test ===");
  Serial.println();

  // Disable timeout during test
  bool savedTimeout = timeoutEnabled;
  timeoutEnabled = false;

  Serial.println("1. Forward...");
  motors.forward();
  delay(1500);
  motors.stop();
  delay(300);

  Serial.println("2. Backward...");
  motors.backward();
  delay(1500);
  motors.stop();
  delay(300);

  Serial.println("3. Turn Left...");
  motors.turnLeft();
  delay(1500);
  motors.stop();
  delay(300);

  Serial.println("4. Turn Right...");
  motors.turnRight();
  delay(1500);
  motors.stop();
  delay(300);

  Serial.println("5. Rotate Left...");
  motors.rotateLeft();
  delay(1500);
  motors.stop();
  delay(300);

  Serial.println("6. Rotate Right...");
  motors.rotateRight();
  delay(1500);
  motors.stop();

  // Restore timeout setting
  timeoutEnabled = savedTimeout;

  Serial.println();
  Serial.println("=== Test Complete ===");
  Serial.println();
}

void printHelp() {
  Serial.println();
  Serial.println("=== Command Reference ===");
  Serial.println();
  Serial.println("Movement:");
  Serial.println("  F          Forward");
  Serial.println("  B          Backward");
  Serial.println("  L          Turn left");
  Serial.println("  R          Turn right");
  Serial.println("  G          Rotate left (spin)");
  Serial.println("  H          Rotate right (spin)");
  Serial.println("  S          Stop");
  Serial.println();
  Serial.println("With speed (180-255):");
  Serial.println("  F:200      Forward at speed 200");
  Serial.println("  B:255      Backward full speed");
  Serial.println();
  Serial.println("Advanced:");
  Serial.println("  J:x:y      Joystick (x,y: -100 to 100)");
  Serial.println("  M:L:R      Manual (signed speeds)");
  Serial.println("  V:speed    Set default speed");
  Serial.println("  ?          Query status");
  Serial.println();
  Serial.println("Utility:");
  Serial.println("  test         Run motor test");
  Serial.println("  timeout on   Enable safety timeout");
  Serial.println("  timeout off  Disable safety timeout");
  Serial.println("  help         Show this help");
  Serial.println();
}
