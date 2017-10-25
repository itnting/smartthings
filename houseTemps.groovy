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
 */
metadata {
	definition (name: "HouseTemps", namespace: "smartthings/testing", author: "doIHaveTo") {
		capability "Button"
        capability "Refresh"
        capability "Thermostat"
                
        command "quickSetHeat"
		command "setTemperature"
        command "setTempUp"
		command "setTempDown"
        command "Refresh"
        command "setToLocModeTemp"
    }

	simulator {

	}
	tiles {
		multiAttributeTile(name:"heatingSetpoint", type: "thermostat", width: 6, height: 4, canChangeIcon: true)
        {
            tileAttribute ("device.heatingSetpoint", key: "PRIMARY_CONTROL") 
            {
                attributeState("default", unit:"dC", label:'${currentValue}°')
            }
            
            tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL") 
            {
                attributeState("VALUE_UP", action: "setTempUp")
                attributeState("VALUE_DOWN", action: "setTempDown")
            }
        }
        
     	valueTile("Home", "device.Home", width: 1, height: 1) {
        	state "Home", label:'Home \n ${currentValue}'
   		}
 	    valueTile("Away", "device.Away", width: 1, height: 1) {
        	state "Away", label:'Away \n ${currentValue}'
   		}
 	    valueTile("Sleep", "device.Sleep", width: 1, height: 1) {
        	state "Sleep", label:'Sleep \n ${currentValue}'
   		}
       	standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", height: 1, width: 1) {
			state "default", label:"Refresh", action:"refresh.refresh", icon: "st.secondary.refresh"
		}
 	     
				
	}
	preferences {
    	section("Location Mode Heating:") {
			input name:"sHome", type:number, title: "Home Temp", description: "Temprature to set when Home... ", required: false, defaultValue:18
        	input name:"sAway", type:number, title: "Away Temp", description: "Temprature to set when Away... ", required: false, defaultValue:18
        	input name:"sSleep", type:number, title: "Sleep Temp", description: "Temprature to set when Sleep... ", required: false, defaultValue:18
        }

    }
     
}

def parse(String description) {
}

def refresh() {
	if (device.currentValue("heatingSetpoint") == null) { quickSetHeat(18) }
	sendEvent(name:"Home", value:sHome)
    sendEvent(name:"Away", value:sAway)
    sendEvent(name:"Sleep", value:sSleep)
    log.trace "Temps Refresh!"
    
}

def updated() {
	if (device.currentValue("heatingSetpoint") == null) { quickSetHeat(18) }
	sendEvent(name:"Home", value:sHome)
    sendEvent(name:"Away", value:sAway)
    sendEvent(name:"Sleep", value:sSleep)
    log.trace "Temps Updated!"
}

def quickSetHeat(degrees) 
{
	setHeatingSetpoint(degrees)
    log.debug("quicksetheat: $degrees")
    updateLocModeTemp(degrees)
}

private updateLocModeTemp(degrees) {
    //def sLocMode = null
    
    def locationMode = location.currentMode
    def sTemp = device.getPreferenceValue("s${locationMode}")
        
	if (sTemp != degrees) {
    	log.debug "Updating ${locationMode} ${sTemp} with ${degrees}"
    	device.updateSetting("s${locationMode}", degrees)
    	refresh()
    }
}
def setToLocModeTemp() {
    def locationMode = location.currentMode
    def sTemp = device.getPreferenceValue("s${locationMode}")

	if (sTemp != degrees) {
    	log.debug "Setting ${device.displayName} ${locationMode} temp to s${sTemp}"
        quickSetHeat(sTemp)
       	refresh()
    }
}

def setTempUp() 
{ 
    def newtemp = device.currentValue("heatingSetpoint").toInteger() + 1
    quickSetHeat(newtemp)
	    
}

def setTempDown() 
{ 
    def newtemp = device.currentValue("heatingSetpoint").toInteger() - 1
    quickSetHeat(newtemp)
}

def setTemperature(temp)
{

	log.debug "setTemperature $temp"
    quickSetHeat(temp)
}

def setHeatingSetpoint(degrees) 
{
	setHeatingSetpoint(degrees.toDouble())
}

def setHeatingSetpoint(Double degrees) 
{
    sendEvent(name: 'heatingSetpoint', value: degrees)

	def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    def locationScale = getTemperatureScale()
    def p = (state.precision == null) ? 1 : state.precision

    def convertedDegrees
    if (locationScale == "C" && deviceScaleString == "F") 
    {
        convertedDegrees = celsiusToFahrenheit(degrees)
    } 
    else if (locationScale == "F" && deviceScaleString == "C") 
    {
        convertedDegrees = fahrenheitToCelsius(degrees)
    } 
    else 
    {
        convertedDegrees = degrees
    }

	log.trace "setHeatingSetpoint scale: $deviceScale precision: $p setpoint: $convertedDegrees"
	state.deviceScale = deviceScale
    state.p = p
    state.convertedDegrees = convertedDegrees
    state.updateNeeded = true
    
    thermostatMode
}


