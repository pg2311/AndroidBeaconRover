# ESP32-C6 Car Controller

Serial-controlled car with serial inputs.

## Project Structure

```
esp32c6_car/
├── esp32c6_car.ino       # Main sketch
├── motor_control.h       # Motor control interface
├── motor_control.cpp     # Motor control implementation
├── command_interface.h   # Command protocol definition
├── command_interface.cpp # Command parser/executor
└── README.md
```

## Hardware Connections

| L298N Pin | ESP32-C6 GPIO |
|-----------|---------------|
| ENA       | GPIO 2        |
| IN1       | GPIO 3        |
| IN2       | GPIO 15       |
| IN3       | GPIO 4        |
| IN4       | GPIO 5        |
| ENB       | GPIO 6        |
| GND       | GND           |

## Setup

1. Put all files in a folder named `esp32c6_car`
2. Open `esp32c6_car.ino` in Arduino IDE
3. Select board: **ESP32C6 Dev Module**
4. Select port
5. Upload

## Usage

1. Open Serial Monitor (115200 baud)
2. Set line ending to **Newline**
3. Type commands

## Commands

### Basic Movement

| Command | Action |
|---------|--------|
| `F` | Forward |
| `B` | Backward |
| `L` | Turn left |
| `R` | Turn right |
| `G` | Rotate left (spin in place) |
| `H` | Rotate right (spin in place) |
| `S` | Stop |

### With Speed (180-255)

| Command | Action |
|---------|--------|
| `F:200` | Forward at speed 200 |
| `F:255` | Forward full speed |
| `B:180` | Backward minimum speed |

### Advanced

| Command | Action |
|---------|--------|
| `J:0:100` | Joystick: full forward |
| `J:50:50` | Joystick: forward-right |
| `J:-100:0` | Joystick: spin left |
| `M:200:-200` | Manual: left fwd, right back |
| `V:200` | Set default speed |
| `?` | Show status |

### Utility

| Command | Action |
|---------|--------|
| `test` | Run automatic motor test |
| `timeout on` | Enable 500ms safety timeout |
| `timeout off` | Disable safety timeout |
| `help` | Show command list |

## Safety Features

- **Command Timeout**: Motors automatically stop after 500ms without commands
- **Minimum Speed**: Speeds below 180 are boosted to prevent motor stall
- **Speed Limits**: All values constrained to valid range

The `CommandInterface` class works with any input source (Serial, BLE, WiFi, etc.)