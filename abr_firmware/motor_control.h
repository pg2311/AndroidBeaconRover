/*
* motor_control.h
 * Motor control interface for L298N driver
 */

#ifndef MOTOR_CONTROL_H
#define MOTOR_CONTROL_H

#include <Arduino.h>

// Pin definitions
#define ENA_PIN  2   // Left motor PWM
#define IN1_PIN  3   // Left motor direction
#define IN2_PIN  15  // Left motor direction
#define IN3_PIN  4   // Right motor direction
#define IN4_PIN  5   // Right motor direction
#define ENB_PIN  6   // Right motor PWM

// PWM settings
#define PWM_FREQ       1500  // 25kHz - silent operation
#define PWM_RESOLUTION 8      // 8-bit: 0-255

// Speed settings
#define MIN_SPEED     100     // Minimum speed that moves motors
#define MAX_SPEED     250
#define DEFAULT_SPEED 195

// Motor identifiers
enum Motor {
    MOTOR_LEFT,
    MOTOR_RIGHT,
    MOTOR_BOTH
  };

// Direction identifiers
enum Direction {
    DIR_STOP,
    DIR_FORWARD,
    DIR_BACKWARD
  };

class MotorControl {
public:
    MotorControl();

    void begin();

    // Individual motor control
    void setLeftMotor(Direction dir, uint8_t speed);
    void setRightMotor(Direction dir, uint8_t speed);

    // Combined control
    void setMotors(Direction leftDir, uint8_t leftSpeed,
                   Direction rightDir, uint8_t rightSpeed);

    // Convenience functions
    void forward(uint8_t speed = DEFAULT_SPEED);
    void backward(uint8_t speed = DEFAULT_SPEED);
    void turnLeft(uint8_t speed = DEFAULT_SPEED);
    void turnRight(uint8_t speed = DEFAULT_SPEED);
    void rotateLeft(uint8_t speed = DEFAULT_SPEED);   // Spin in place
    void rotateRight(uint8_t speed = DEFAULT_SPEED);  // Spin in place
    void stop();

    // Speed adjustment
    void setSpeed(uint8_t speed);
    uint8_t getSpeed() const;

    // Status
    bool isMoving() const;

private:
    uint8_t currentSpeed;
    bool moving;

    uint8_t constrainSpeed(uint8_t speed);
    void applyLeftMotor(Direction dir, uint8_t speed);
    void applyRightMotor(Direction dir, uint8_t speed);
};

#endif // MOTOR_CONTROL_H