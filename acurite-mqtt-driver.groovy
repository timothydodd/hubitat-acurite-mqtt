/*
 *  Acurite MQTT Sensor
 *  Device Driver for Hubitat Elevation hub
 * https://www.robododd.com
 * Control Acurite Mqtt temp
 * 2023-09-05
 */
metadata {
    definition (name: "Acurite MQTT", namespace: "dodd", author: "Tim Dodd") {
        capability "Initialize"
        capability "Sensor"
        capability "TemperatureMeasurement"
        capability "Relative Humidity Measurement"
    }
}

preferences {
    section("URIs") {
        input "mqttBroker", "string", title: "MQTT Broker Address:Port", required: true
		input "mqttTopic", "string", title: "MQTT Topic", description: "(e.g. test/Acurite-Tower/9932)", required: true
        input "tempSmoothing", "integer", title: "Temperature smoothing", description: "Number of readings to average for smoother temperature changes", required: true, defaultValue: 5
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}




def installed() {
    logDebug "Installed"
}

def parse(String description) {
    //logDebug description
	
    mqtt = interfaces.mqtt.parseMessage(description)
	//logDebug mqtt
	
	json = new groovy.json.JsonSlurper().parseText(mqtt.payload)
	logDebug json

    def events = [:]
    

        // temperature reading
     def temp = smoothenTemperatureChange(convertCelciusToLocalTemp(json.temperature_C))

     events.temperature = [name: 'temperature', value: temp, unit: "°${location.temperatureScale}", descriptionText: "Temperature is ${temp}°${location.temperatureScale}", translatable:true]
     events.humidity = [name: 'humidity', value: json.humidity, unit: "%", descriptionText: "Humidity is ${json.humidity}%", translatable:true]

    events.each {
        sendEvent(it.value)
    }
}

def smoothenTemperatureChange(temp) {
    // average the temperature to avoid jumping up and down between two values, e.g. between 22 and 23 when temperature is 22.5
    def averageTemp = state.averageTemperature ? state.averageTemperature : temp
    def smoothing = tempSmoothing ? tempSmoothing.toInteger() : 5
    averageTemp = Math.round((temp + averageTemp * smoothing) / (smoothing + 1) * 1000000000)/1000000000
    state.averageTemperature = averageTemp
    temp = Math.round(averageTemp)
}


def convertCelciusToLocalTemp(temp) {
    return (location.temperatureScale == "F") ? ((temp * 1.8) + 32) : temp
}

def convertLocalToCelsiusTemp(temp) {
    return (location.temperatureScale == "F") ? Math.round((temp - 32) / 1.8) : temp
}

def updated() {
    logDebug "Updated"
    
    initialize()
}

def uninstalled() {
    logDebug "Uninstalled"
    disconnect()
}

def disconnect() {
    log.info "Disconnecting from MQTT"
    interfaces.mqtt.unsubscribe(settings.mqttTopic)
    interfaces.mqtt.disconnect()
}

def delayedConnect() {
    // increase delay by 5 seconds every time, to max of 1 hour
    if (state.delay < 3600)
        state.delay = (state.delay ?: 0) + 5

    logDebug "Reconnecting in ${state.delay}s"
    runIn(state.delay, connect)
}

def initialize() {
    logDebug "Initialize"
    state.delay = 0
    connect()
}

def connect() {
    try {
        // open connection
        log.info "Connecting to ${settings.mqttBroker}"
        interfaces.mqtt.connect("tcp://" + settings.mqttBroker, "hubitat_acurite_${device.id}", null, null)
        // subscribe once received connection succeeded status update below        
    } catch(e) {
        log.error "MQTT Connect error: ${e.message}."
        delayedConnect()
    }
}

def subscribe() {
    interfaces.mqtt.subscribe(settings.mqttTopic)
    logDebug "Subscribed to topic ${settings.mqttTopic}"
}

def mqttClientStatus(String status){
    // This method is called with any status messages from the MQTT client connection (disconnections, errors during connect, etc) 
    // The string that is passed to this method with start with "Error" if an error occurred or "Status" if this is just a status message.
    def parts = status.split(': ')
    switch (parts[0]) {
        case 'Error':
            log.warn status
            switch (parts[1]) {
                case 'Connection lost':
                case 'send error':
                    state.delay = 0
                    delayedConnect()
                    break
            }
            break
        case 'Status':
            log.info "MQTT ${status}"
            switch (parts[1]) {
                case 'Connection succeeded':
                    state.remove('delay')
                    // without this delay the `parse` method is never called
                    // (it seems that there needs to be some delay after connection to subscribe)
                    runInMillis(1000, subscribe)
                    break
            }
            break
        default:
            logDebug "MQTT ${status}"
            break
    }
}

def logInfo(msg) {
    if (logEnable) log.info msg
}

def logDebug(msg) {
    if (logEnable) log.debug msg
}
