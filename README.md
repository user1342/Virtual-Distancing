# Digital distancing

A simple game with one aim, don't get infected. 

The infection spreadsbetween Android devices with Wi-Fi enabled and follows the below rules:
- It can transmit to devices up to 200 meteres away.
- It takes around a minute to contract in proximity. 
- Once contracted it takes two weeks to be cured. 

# The Internals
This application uses WiFi P2P to find nearby Network Services. A device can be in one of two states, infected and clean. An infected device sets up a network service broadcasting that it is infected, while a clean device searches for these network services and if it finds one then also becomes infected. A foreground service is setup which listens for these nearby Network Services.

## Self-infection
To get the ball rolling you can self infect your device by clicking on the screen 5 times. 

## Recovery
After becomming infected a device is cured after two weeks. Is is done via an Alarm Receiver which is setup when the device is infected. 
