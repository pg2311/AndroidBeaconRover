# ABR (Android Beacon Rover)

**ABR** is a distributed remote control system that uses multiple Android smartphones as a sensor array to control an ESP32 vehicle. It utilizes "Sensor Fusion" by aggregating BLE RSSI data from multiple "Guest" phones to calculate control vectors on a central "Host" phone.

## Architecture

The system operates on a Star Topology centered around the Host Smartphone.

* **Vehicle (ESP32):** Acts as a BLE Beacon and Motor Controller.
* **Guest Phones:** Act as distributed sensors. They scan the Vehicle's signal strength (RSSI) and stream binary packets to the Host.
* **Host Phone:** Acts as the Wi-Fi Hotspot, UDP Server, and Central Brain. It fuses sensor data and sends steering commands to the Vehicle.
