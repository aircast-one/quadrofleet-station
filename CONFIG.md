# QuadroFleet Setup

**Instructions for setting up the QuadroFleet.**

---

## 1. Prepare the SD Card

1. Download the [QuadroFleet image](https://drive.google.com/file/d/1OjDPq93CbZM0tucmNRlkNYryCWIlYzS-/view).
2. Flash the image to an SD card using the [Raspberry Pi Imager](https://www.raspberrypi.com/software/).
   - Select Custom image
   - Select SD Card
   - Flash

---

## 2. Install ext4 File Manager for Windows (only for Windows users)

To access ext4 partitions on Windows,
download and install [Paragon Linux File Systems for Windows](https://dl.paragon-software.com/demo/linuxwin7_trial.msi).

---

## 3. Get an OpenVPN Account

1. **Register** for a new [OpenVPN account](https://myaccount.openvpn.com/signup/cvpn).
2. Update your **settings** as needed.
3. Create **Raspberry Pi devices** in your account.
4. Download the **profile file**.
5. Install [OpenVPN Client](https://openvpn.net/client/) for managing connections.

---

## 4. Prepare Data for Configuration Files

1. Generate WPA Passphrase for Wi-Fi connection (Optional):

   Use the `wpa_passphrase` tool or [online generator](https://www.wireshark.org/tools/wpa-psk.html) to create a secure Wi-Fi passphrase.

   You can do it in Windows WSL. 

   - How to launch it

   Install `wpasupplicant` if needed:

   ```bash
   sudo apt install wpasupplicant -y
   ```

   Generate the passphrase:

   ```bash
   wpa_passphrase WIFI_SSID_NAME WIFI_PASSPHRASE
   ```

2. Retrieve OpenVPN IP Addresses:

   Get the necessary IP addresses from your OpenVPN connection for further configuration.

3. List of prepared information:
   
   - OpenVPN profile
   - ...
   
---

## 5. Update Data on the SD Card

1. Insert the SD Card into your Card Reader.

2. Update Wi-Fi Settings (Optional):

   If you configure Wi-Fi then you can connect to Raspberry Pi but traffic still will be processed by 4G modem if you do not deactivate it manually.

   Edit the Wi-Fi connection configuration file:

   ```path
   /etc/NetworkManager/system-connections/preconfigured.nmconnection
   ```

   Replace the placeholder values in the file with your Wi-Fi details:

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

3. Update Host IP Addresses:

   Replace placeholders with the correct IP addresses obtained from OpenVPN.

   - list of files

4. Update OpenVPN Configuration:

   Add the required values, such as your OpenVPN credentials and server addresses, to the relevant configuration files.

   - list of files

---

## 6. Connect to Raspberry Pi via Wi-Fi (for ssh or short-range control)

If you plan to use only SIM card, you can skip this step.

1. Power on the Raspberry Pi.
2. Connect to the Raspberry Pi using its Wi-Fi network.
3. Deactivate the E3372 modem to change traffic routing. After reboot, it will be *automatically* restored:

   ```bash
   sudo nmcli connection show
   sudo nmcli connection down "Wired connection 1"
   ```
---

## 7. QuadroFleet Station setup

1. Install [QuadroFleet Station software](https://google.com).
2. Install [GStreamer](https://gstreamer.freedesktop.org/download/)
3. Install [Java Amazon Corretto](https://corretto.aws/downloads/latest/amazon-corretto-21-x64-windows-jdk.msi)

---

## 8. Use QuadroFleet via SIM Card

1. Launch **OpenVPN Connect** to establish a secure connection.
2. Start the **QuadroFleet Station** software.
3. Insert SIM Card and check the amount of available traffic on it
4. Power the FPV drone.
5. Gamepad automatically detected
6. Control buttons
