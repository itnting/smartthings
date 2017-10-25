/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 *  Thermostat Monitor
 *  Author: doIHaveTo
 */
definition(
    name: "ThermostatMonitor2",
    namespace: "doIHaveTo",
    author: "doIHaveTo",
    description: "Monitor the thermostat mode and turn on a switch when needed.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
	section("Monitor mode of which thermostat of...") {
		input "temperatureSensor", "capability.thermostat"
	}
	section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phone1", "phone", title: "Send a Text Message?", required: false
        }
    }
	section("Turn on things...") {
		input "conSwitches", "capability.switch", required: false, multiple:true
	}
}

def installed() {
	subscribe(temperatureSensor, "thermostatMode", thermostatModeHandler)
}

def updated() {
	unsubscribe()
    subscribe(temperatureSensor, "thermostatMode", thermostatModeHandler)
}

def thermostatModeHandler(evt) {
	log.debug "Thermostat Mode Changed: $evt.value"
	def edn = evt.displayName
    
    if (evt.value == "heat") {
	    def currSwitches = conSwitches.currentSwitch
        def offSwitches = currSwitches.findAll { it == "off" ? true : false }
               
        if (offSwitches.size() != 0) {
        		log.debug "Heat:On, activating ${conSwitches}"
                conSwitches.on()
                send("${evt.value} Heat:On")
    	}
    }
    else if (evt.value == "off") {
    	def currSwitches = conSwitches.currentSwitch
    	def onSwitches = currSwitches.findAll { it == "on" }
                
        if (onSwitches.size() != 0) {
        	log.debug "Heat:off de-activating ${conSwitches}"
            conSwitches.off()
            send("${evt.value} H:Off")
        }
    }
}

private send(msg) {
    if (location.contactBookEnabled) {
        log.debug("sending notifications to: ${recipients?.size()}")
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage != "No") {
            log.debug("sending push message")
            sendPush(msg)
        }

        if (phone1) {
            log.debug("sending text message")
            sendSms(phone1, msg)
        }
    }

    log.debug msg
}
