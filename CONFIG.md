# QuadroFleet Control Setup

Instructions for setup QuadroFleet image.

## 1. Prepare SD Card

Download [QuadroFleet image](https://google.com) and flash it to SD card via [Raspberry Pi Imager](https://www.raspberrypi.com/software/).

## 2. Install ext4 file manager for Windows (optional)

Download and install [Paragon Linux File Systems for Windows](https://dl.paragon-software.com/demo/linuxwin7_trial.msi).

## 3. Get OpenVPN Account

1. Register a new account
2. Update settings
3. Create devices
4. Download profile
5. Install OpenVPN on your PC

## 4. Prepare data for configuration files

1. Generate WPS Passphrase by tool or [Online](https://www.wireshark.org/tools/wpa-psk.html)
   
   ```bash
   sudo apt install wpasupplicant -y
   ```

   ```bash
   wpa_passphrase WIFI_SSID_NAME WIFI_PASSPHRASE
   ```

2. Get IP addresses from OpenVPN

## 5. Update data on SD Card

1. Insert SD Card
2. Update Wi-Fi settings (optional)

   ```path
   /etc/NetworkManager/system-connections/preconfigured.nmconnection
   ```

   ```ini
   [connection]
   id=preconfigured
   uuid=2273747d-9d68-4fa3-ba00-4574755c49b4
   type=wifi
   [wifi]
   mode=infrastructure
   ssid=QuadroFleet <<< Replace with your Wi-Fi SSID
   hidden=false
   [ipv4]
   method=auto
   [ipv6]
   addr-gen-mode=default
   method=auto
   [proxy]
   [wifi-security]
   key-mgmt=wpa-psk
   psk=d1269361da09aed23b4e3b9cd9f2972d0440df281405d5943f2c741c9c4d50e6 <<< Replace with your Wi-Fi Passphrase hash
   ```

3. Update host IP addresses
4. Update OpenVPN values 

## 6. Connect to Raspberry Pi via Wi-Fi

## 7. Use QuadroFleet Station on PC

1. Download [QuadroFleet Station](https://google.com).
2. Download and install [GStreamer](https://gstreamer.freedesktop.org/download/).
3. Download and install [Java Amazon Corretto](https://corretto.aws/downloads/latest/amazon-corretto-21-x64-windows-jdk.msi).

## 8. Use QuadroFleet via SIM Card

1. Launch OpenVPN
2. Launch QuadroFleet Station
3. Power FPV drone with QuadroFleet Control device