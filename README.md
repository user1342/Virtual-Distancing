# Virtual Distancing

<img align="right" width="150" height="150" src="media\heart.png">

A simple geo-based game and educational tool about infection. The game has one aim... don't get infected.

The infection spreads between Android devices with **Wi-Fi enabled** and follows the below rules:
- It can transmit to devices up to 200 meters away.
- It can take up to a minute to contract.
- Once contracted it takes two weeks to be cured.

Virtual Distancing is a geo-location based game and educational
tool designed around the concept of infection. This uses the Wifi
Direct (a.k.a WiFiP2P) technology.


<p align="center"><img src="media\infection.gif" alt="Install Blocked Notification" width="400"/></p>

## The Internals
This application uses [WiFi Direct](https://developer.android.com/training/connect-devices-wirelessly/wifi-direct) (Also known as WiFiP2P) to find nearby devices up to 200 meters away. A device can be in one of two states, infected and clean. An infected device sets up a WifiP2P network service broadcasting that it is infected, while a clean device searches for these network services and if it finds one then also becomes infected. A foreground service is setup which listens for these nearby Network Services.

### Self-infection
To get the ball rolling you can self-infect your device by clicking on the screen 5 times.

### Recovery
After becoming infected a device is cured after two weeks. This is done via an Alarm Receiver which is setup when the device is infected.

## Thanks
Thanks to [pixelsapphire](https://pixelsapphire.itch.io/pixel-16x16-heart) for the logo.

## License
[GNU General Public License v3.0](https://choosealicense.com/licenses/gpl-3.0/)
