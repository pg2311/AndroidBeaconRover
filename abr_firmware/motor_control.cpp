/*
 * motor_control.cpp
 * Motor control implementation for L298N driver
 */

#include "motor_control.h"

MotorControl::MotorControl()
  : currentSpeed(DEFAULT_SPEED), moving(false) {
}

void MotorControl::begin() {
  // Configure direction pins
  pinMode(IN1_PIN, OUTPUT);
  pinMode(IN2_PIN, OUTPUT);
  pinMode(IN3_PIN, OUTPUT);
  pinMode(IN4_PIN, OUTPUT);

  // Configure PWM pins
  ledcAttach(ENA_PIN, PWM_FREQ, PWM_RESOLUTION);
  ledcAttach(ENB_PIN, PWM_FREQ, PWM_RESOLUTION);

  // Start stopped
  stop();

  Serial.println("[Motor] Initialized");
}

uint8_t MotorControl::constrainSpeed(uint8_t speed) {
  if (speed == 0) return 0;
  if (speed < MIN_SPEED) return MIN_SPEED;
  if (speed > MAX_SPEED) return MAX_SPEED;
  return speed;
}

void MotorControl::applyLeftMotor(Direction dir, uint8_t speed) {
  speed = constrainSpeed(speed);

  switch (dir) {
    case DIR_FORWARD:
      digitalWrite(IN1_PIN, HIGH);
      digitalWrite(IN2_PIN, LOW);
      ledcWrite(ENA_PIN, speed);
      break;
    case DIR_BACKWARD:
      digitalWrite(IN1_PIN, LOW);
      digitalWrite(IN2_PIN, HIGH);
      ledcWrite(ENA_PIN, speed);
      break;
    case DIR_STOP:
    default:
      digitalWrite(IN1_PIN, LOW);
      digitalWrite(IN2_PIN, LOW);
      ledcWrite(ENA_PIN, 0);
      break;
  }
}

void MotorControl::applyRightMotor(Direction dir, uint8_t speed) {
  speed = constrainSpeed(speed);

  switch (dir) {
    case DIR_FORWARD:
      digitalWrite(IN3_PIN, HIGH);
      digitalWrite(IN4_PIN, LOW);
      ledcWrite(ENB_PIN, speed);
      break;
    case DIR_BACKWARD:
      digitalWrite(IN3_PIN, LOW);
      digitalWrite(IN4_PIN, HIGH);
      ledcWrite(ENB_PIN, speed);
      break;
    case DIR_STOP:
    default:
      digitalWrite(IN3_PIN, LOW);
      digitalWrite(IN4_PIN, LOW);
      ledcWrite(ENB_PIN, 0);
      break;
  }
}

void MotorControl::setLeftMotor(Direction dir, uint8_t speed) {
  applyLeftMotor(dir, speed);
  moving = (dir != DIR_STOP);
}

void MotorControl::setRightMotor(Direction dir, uint8_t speed) {
  applyRightMotor(dir, speed);
  moving = (dir != DIR_STOP);
}

void MotorControl::setMotors(Direction leftDir, uint8_t leftSpeed,
                              Direction rightDir, uint8_t rightSpeed) {
  applyLeftMotor(leftDir, leftSpeed);
  applyRightMotor(rightDir, rightSpeed);
  moving = (leftDir != DIR_STOP) || (rightDir != DIR_STOP);
}

void MotorControl::forward(uint8_t speed) {
  setMotors(DIR_FORWARD, speed, DIR_FORWARD, speed);
  Serial.printf("[Motor] Forward @ %d\n", speed);
}

void MotorControl::backward(uint8_t speed) {
  setMotors(DIR_BACKWARD, speed, DIR_BACKWARD, speed);
  Serial.printf("[Motor] Backward @ %d\n", speed);
}

void MotorControl::turnLeft(uint8_t speed) {
  // Left motor slower, right motor faster
  setMotors(DIR_FORWARD, speed / 2, DIR_FORWARD, speed);
  Serial.printf("[Motor] Turn Left @ %d\n", speed);
}

void MotorControl::turnRight(uint8_t speed) {
  // Left motor faster, right motor slower
  setMotors(DIR_FORWARD, speed, DIR_FORWARD, speed / 2);
  Serial.printf("[Motor] Turn Right @ %d\n", speed);
}

void MotorControl::rotateLeft(uint8_t speed) {
  // Spin in place: left backward, right forward
  setMotors(DIR_BACKWARD, speed, DIR_FORWARD, speed);
  Serial.printf("[Motor] Rotate Left @ %d\n", speed);
}

void MotorControl::rotateRight(uint8_t speed) {
  // Spin in place: left forward, right backward
  setMotors(DIR_FORWARD, speed, DIR_BACKWARD, speed);
  Serial.printf("[Motor] Rotate Right @ %d\n", speed);
}

void MotorControl::stop() {
  setMotors(DIR_STOP, 0, DIR_STOP, 0);
  moving = false;
  Serial.println("[Motor] Stop");
}

void MotorControl::setSpeed(uint8_t speed) {
  currentSpeed = constrainSpeed(speed);
}

uint8_t MotorControl::getSpeed() const {
  return currentSpeed;
}

bool MotorControl::isMoving() const {
  return moving;
}