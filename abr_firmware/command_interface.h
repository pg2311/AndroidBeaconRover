/*
 * command_interface.h
 * Command protocol definition and parser
 *
 * Command Format (simple text-based protocol):
 * Single character commands:
 *   F - Forward
 *   B - Backward
 *   L - Turn Left
 *   R - Turn Right
 *   G - Rotate Left (spin)
 *   H - Rotate Right (spin)
 *   S - Stop
 *
 * With parameters (colon-separated):
 *   F:200     - Forward at speed 200
 *   F:200:100 - Forward at speed 200 for 100ms
 *   B:180     - Backward at speed 180
 *   B:180:100 - Backward at speed 180 for 100ms
 *   M:200:220 - Manual mode: left speed, right speed (signed: negative = backward)
 *   V:200     - Set default speed to 200
 *   G:100     - Rotate Left for 100ms
 *   H:100     - Rotate Right for 100ms
 *
 * Joystick mode (for smooth Android control):
 *   J:x:y     - Joystick input where x,y are -100 to 100
 *               x = left/right, y = forward/backward
 */

#ifndef COMMAND_INTERFACE_H
#define COMMAND_INTERFACE_H

#include <Arduino.h>
#include "motor_control.h"

// Command types
enum CommandType {
  CMD_NONE,
  CMD_FORWARD,
  CMD_BACKWARD,
  CMD_TURN_LEFT,
  CMD_TURN_RIGHT,
  CMD_ROTATE_LEFT,
  CMD_ROTATE_RIGHT,
  CMD_STOP,
  CMD_MANUAL,       // Direct motor control
  CMD_JOYSTICK,     // Joystick x,y input
  CMD_SET_SPEED,    // Set default speed
  CMD_QUERY,        // Query status
  CMD_INVALID
};

// Parsed command structure
struct Command {
  CommandType type;
  int16_t param1;   // Speed or X value
  int16_t param2;   // Y value (for joystick/manual)
  bool hasParams;
};

class CommandInterface {
public:
  CommandInterface(MotorControl* motors);

  void begin();

  // Parse a command string
  Command parse(const String& input);

  // Execute a parsed command
  void execute(const Command& cmd);

  // Parse and execute in one step
  void process(const String& input);

  // Get status string for query response
  String getStatus() const;

  // Check timed moves
  void update();

private:
  MotorControl* motors;
  uint8_t defaultSpeed;
  Command lastCommand;

  unsigned long moveEndTime;
  bool timedMoveActive;

  // Joystick mixing algorithm
  void processJoystick(int16_t x, int16_t y);

  // Parse helper
  int16_t parseNumber(const String& str, int startIndex, int endIndex);
};

#endif // COMMAND_INTERFACE_H