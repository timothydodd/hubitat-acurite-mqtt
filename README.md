# Acurite MQTT Sensor Driver for Hubitat

This is a custom device driver for Hubitat Elevation that integrates Acurite MQTT sensors. It provides real-time temperature and humidity readings using MQTT.

## Features

- **Temperature Measurement**: Reads temperature from an Acurite MQTT sensor and smooths fluctuations using a configurable averaging system.
- **Humidity Measurement**: Reads and reports humidity values.
- **MQTT Integration**: Connects to an MQTT broker to receive sensor data.
- **Smoothing**: Configurable temperature smoothing to avoid abrupt temperature changes in readings.

## Requirements

- **Hubitat Elevation**: Ensure your Hubitat hub is running the latest firmware.
- **MQTT Broker**: You need an MQTT broker (such as Mosquitto) and an Acurite sensor publishing data via MQTT.

## Installation

1. Copy the driver code from the [Acurite MQTT Sensor Driver](https://github.com/your-repo-link) repository.
2. Open the Hubitat web interface and navigate to **Drivers Code**.
3. Click **New Driver** and paste the copied code.
4. Save the driver and name it `Acurite MQTT`.
5. Add a new device using this driver in **Devices** > **Add Virtual Device**.
6. Configure the device with the appropriate MQTT settings.

## Configuration

### Device Preferences

- **MQTT Broker Address:Port**: The address and port of your MQTT broker (e.g., `192.168.1.100:1883`).
- **MQTT Topic**: The MQTT topic from which the Acurite sensor is publishing data (e.g., `test/Acurite-Tower/9932`).
- **Temperature Smoothing**: The number of readings to average for smoother temperature changes. Default: 5.
- **Enable Debug Logging**: Toggle to enable or disable debug logs.

### MQTT Configuration Example

```yaml
mqttBroker: "192.168.1.100:1883"
mqttTopic: "test/Acurite-Tower/9932"
tempSmoothing: 5
logEnable: true
```
## Driver Details
This driver subscribes to the specified MQTT topic to receive temperature and humidity data. It automatically converts Celsius to Fahrenheit based on the Hubitat location settings. The driver also smooths out rapid temperature fluctuations by averaging readings based on a user-defined smoothing factor.

## Key Methods
- connect(): Connects to the MQTT broker.
- subscribe(): Subscribes to the MQTT topic for sensor data.
- smoothenTemperatureChange(temp): Averages temperature readings to prevent rapid fluctuations.
- mqttClientStatus(String status): Handles MQTT connection status and errors.
- parse(String description): Processes incoming MQTT messages, parses the JSON payload, and updates temperature and humidity attributes.

## Debugging
Enable debug logging in the device preferences to get more detailed logs in the Hubitat logs section. This will help troubleshoot MQTT connection issues and sensor data parsing.

## Contributing
Feel free to fork this repository and submit pull requests to contribute to this project.

##Disclaimer
This project is not affiliated with or endorsed by Acurite. 

## License
This project is licensed under the MIT License. See the LICENSE file for more details.

Author: Tim Dodd
Website: https://www.robododd.com
