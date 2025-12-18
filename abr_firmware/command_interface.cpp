/*
 * command_interface.cpp
 * Command protocol implementation
 */
 
#include "command_interface.h"

CommandInterface::CommandInterface(MotorControl* motors)
  : motors(motors), defaultSpeed(DEFAULT_SPEED),
  moveEndTime(0), timedMoveActive(false) {
  lastCommand = {CMD_NONE, 0, 0, false};
}

void CommandInterface::begin() {
  Serial.printf("[Command] Interface initialized\n");
}

int16_t CommandInterface::parseNumber(const String& str, int startIndex, int endIndex) {
  if (startIndex >= str.length()) return 0;
  if (endIndex < 0) endIndex = str.length();

  String numStr = str.substring(startIndex, endIndex);
  numStr.trim();
  return numStr.toInt();
}

Command CommandInterface::parse(const String& input) {
  Command cmd = {CMD_NONE, 0, 0, false};

  if (input.length() == 0) {
    cmd.type = CMD_INVALID;
    return cmd;
  }

  String trimmed = input;
  trimmed.trim();
  trimmed.toUpperCase();

  char cmdChar = trimmed.charAt(0);

  // Find parameter positions
  int colon1 = trimmed.indexOf(':');
  int colon2 = (colon1 > 0) ? trimmed.indexOf(':', colon1 + 1) : -1;

  // Parse based on command character
  switch (cmdChar) {
    case 'F':
      cmd.type = CMD_FORWARD;
      if (colon1 > 0 && colon2 > 0) {
        cmd.param1 = parseNumber(trimmed, colon1 + 1, colon2);
        cmd.param2 = parseNumber(trimmed, colon2 + 1, -1);
        cmd.hasParams = true;
      } else if (colon1 > 0) {
        cmd.param1 = parseNumber(trimmed, colon1 + 1, -1);
        cmd.hasParams = true;
      }
      break;

    case 'B':
      cmd.type = CMD_BACKWARD;
      if (colon1 > 0 && colon2 > 0) {
        cmd.param1 = parseNumber(trimmed, colon1 + 1, colon2);
        cmd.param2 = parseNumber(trimmed, colon2 + 1, -1);
        cmd.hasParams = true;
      } else if (colon1 > 0) {
        cmd.param1 = parseNumber(trimmed, colon1 + 1, -1);
        cmd.hasParams = true;
      }
      break;

    case 'L':
      cmd.type = CMD_TURN_LEFT;
      if (colon1 > 0 && colon2 > 0) {
        cmd.param1 = parseNumber(trimmed, colon1 + 1, colon2);
        cmd.param2 = parseNumber(trimmed, colon2 + 1, -1);
        cmd.hasParams = true;
      } else if (colon1 > 0) {
        cmd.param1 = parseNumber(trimmed, colon1 + 1, -1);
        cmd.hasParams = true;
      }
      break;

    case 'R':
      cmd.type = CMD_TURN_RIGHT;
      if (colon1 > 0 && colon2 > 0) {
        cmd.param1 = parseNumber(trimmed, colon1 + 1, colon2);
        cmd.param2 = parseNumber(trimmed, colon2 + 1, -1);
        cmd.hasParams = true;
      } else if (colon1 > 0) {
        cmd.param1 = parseNumber(trimmed, colon1 + 1, -1);
        cmd.hasParams = true;
      }
      break;

    case 'G':
      cmd.type = CMD_ROTATE_LEFT;
      if (colon1 > 0 && colon2 > 0) {
        cmd.param1 = parseNumber(trimmed, colon1 + 1, colon2);
        cmd.param2 = parseNumber(trimmed, colon2 + 1, -1);
        cmd.hasParams = true;
      } else if (colon1 > 0) {
        cmd.param1 = parseNumber(trimmed, colon1 + 1, -1);
        cmd.hasParams = true;
      }
      break;

    case 'H':
      cmd.type = CMD_ROTATE_RIGHT;
      if (colon1 > 0 && colon2 > 0) {
        cmd.param1 = parseNumber(trimmed, colon1 + 1, colon2);
        cmd.param2 = parseNumber(trimmed, colon2 + 1, -1);
        cmd.hasParams = true;
      } else if (colon1 > 0) {
        cmd.param1 = parseNumber(trimmed, colon1 + 1, -1);
        cmd.hasParams = true;
      }
      break;

    case 'S':
      cmd.type = CMD_STOP;
      break;

    case 'M':  // Manual: M:leftSpeed:rightSpeed
      cmd.type = CMD_MANUAL;
      if (colon1 > 0 && colon2 > 0) {
        cmd.param1 = parseNumber(trimmed, colon1 + 1, colon2);
        cmd.param2 = parseNumber(trimmed, colon2 + 1, -1);
        cmd.hasParams = true;
      } else {
        cmd.type = CMD_INVALID;
      }
      break;

    case 'J':  // Joystick: J:x:y
      cmd.type = CMD_JOYSTICK;
      if (colon1 > 0 && colon2 > 0) {
        cmd.param1 = parseNumber(trimmed, colon1 + 1, colon2);
        cmd.param2 = parseNumber(trimmed, colon2 + 1, -1);
        cmd.hasParams = true;
      } else {
        cmd.type = CMD_INVALID;
      }
      break;

    case 'V':  // Set speed: V:speed
      cmd.type = CMD_SET_SPEED;
      if (colon1 > 0) {
        cmd.param1 = parseNumber(trimmed, colon1 + 1, -1);
        cmd.hasParams = true;
      } else {
        cmd.type = CMD_INVALID;
      }
      break;

    default:
      cmd.type = CMD_INVALID;
      break;
  }

  return cmd;
}

void CommandInterface::processJoystick(int16_t x, int16_t y) {
  // x: -100 (left) to +100 (right)
  // y: -100 (backward) to +100 (forward)

  // Dead zone
  if (abs(x) < 10 && abs(y) < 10) {
    motors->stop();
    return;
  }

  // Tank-style mixing
  // Convert joystick to differential drive

  int16_t scaledX = map(x, -100, 100, -255, 255);
  int16_t scaledY = map(y, -100, 100, -255, 255);

  int16_t leftSpeed = scaledY + scaledX;
  int16_t rightSpeed = scaledY - scaledX;

  // Constrain
  leftSpeed = constrain(leftSpeed, -255, 255);
  rightSpeed = constrain(rightSpeed, -255, 255);

  // Determine directions and speeds
  Direction leftDir = DIR_STOP;
  Direction rightDir = DIR_STOP;
  uint8_t leftSpeedAbs = 0;
  uint8_t rightSpeedAbs = 0;

  if (leftSpeed > 0) {
    leftDir = DIR_FORWARD;
    leftSpeedAbs = leftSpeed;
  } else if (leftSpeed < 0) {
    leftDir = DIR_BACKWARD;
    leftSpeedAbs = -leftSpeed;
  }

  if (rightSpeed > 0) {
    rightDir = DIR_FORWARD;
    rightSpeedAbs = rightSpeed;
  } else if (rightSpeed < 0) {
    rightDir = DIR_BACKWARD;
    rightSpeedAbs = -rightSpeed;
  }

  motors->setMotors(leftDir, leftSpeedAbs, rightDir, rightSpeedAbs);

  Serial.printf("[Command] Joystick x=%d y=%d -> L:%d R:%d\n",
                x, y, leftSpeed, rightSpeed);
}

void CommandInterface::execute(const Command& cmd) {
  uint8_t speed = cmd.hasParams ? cmd.param1 : defaultSpeed;
  bool isTimedCommand = cmd.hasParams && cmd.param2 > 0;

  switch (cmd.type) {
    case CMD_FORWARD:
      motors->forward(speed);
      if (isTimedCommand) {
        timedMoveActive = true;
        moveEndTime = millis() + cmd.param2;
        Serial.printf("[Command] Forward speed=%d for %dms\n", speed, cmd.param2);
      }
      break;

    case CMD_BACKWARD:
      motors->backward(speed);
      if (isTimedCommand) {
        timedMoveActive = true;
        moveEndTime = millis() + cmd.param2;
        Serial.printf("[Command] Backward speed=%d for %dms\n", speed, cmd.param2);
      }
      break;

    case CMD_TURN_LEFT:
      motors->turnLeft(speed);
      if (isTimedCommand) {
        timedMoveActive = true;
        moveEndTime = millis() + cmd.param2;
        Serial.printf("[Command] Turn left speed=%d for %dms\n", speed, cmd.param2);
      }
      break;

    case CMD_TURN_RIGHT:
      motors->turnRight(speed);
      if (isTimedCommand) {
        timedMoveActive = true;
        moveEndTime = millis() + cmd.param2;
        Serial.printf("[Command] Turn right speed=%d for %dms\n", speed, cmd.param2);
      }
      break;

    case CMD_ROTATE_LEFT:
      motors->rotateLeft(speed);
      if (isTimedCommand) {
        timedMoveActive = true;
        moveEndTime = millis() + cmd.param2;
        Serial.printf("[Command] Rotate left speed=%d for %dms\n", speed, cmd.param2);
      }
      break;

    case CMD_ROTATE_RIGHT:
      motors->rotateRight(speed);
      if (isTimedCommand) {
        timedMoveActive = true;
        moveEndTime = millis() + cmd.param2;
        Serial.printf("[Command] Rotate right speed=%d for %dms\n", speed, cmd.param2);
      }
      break;

    case CMD_STOP:
      motors->stop();
      timedMoveActive = false;
      break;

    case CMD_MANUAL: {
      // param1 = left, param2 = right (signed values)
      Direction leftDir = (cmd.param1 >= 0) ? DIR_FORWARD : DIR_BACKWARD;
      Direction rightDir = (cmd.param2 >= 0) ? DIR_FORWARD : DIR_BACKWARD;
      motors->setMotors(leftDir, abs(cmd.param1), rightDir, abs(cmd.param2));
      timedMoveActive = false;
      Serial.printf("[Command] Manual L:%d R:%d\n", cmd.param1, cmd.param2);
      break;
    }

    case CMD_JOYSTICK:
      processJoystick(cmd.param1, cmd.param2);
      timedMoveActive = false;
      break;

    case CMD_SET_SPEED:
      defaultSpeed = constrain(cmd.param1, MIN_SPEED, MAX_SPEED);
      Serial.printf("[Command] Speed set to %d\n", defaultSpeed);
      break;

    case CMD_INVALID:
      Serial.printf("[Command] Invalid command\n");
      break;

    default:
      break;
  }

  lastCommand = cmd;
}

void CommandInterface::update() {
  if (timedMoveActive && millis() >= moveEndTime) {
    motors->stop();
    timedMoveActive = false;
    Serial.printf("[Command] Timed move completed\n");
  }
}

void CommandInterface::process(const String& input) {
  Command cmd = parse(input);
  execute(cmd);
}

String CommandInterface::getStatus() const {
  String status = "STATUS:";
  status += motors->isMoving() ? "MOVING" : "STOPPED";
  status += ":";
  status += String(defaultSpeed);
  return status;
}