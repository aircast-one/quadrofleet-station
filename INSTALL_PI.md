# QuadroFleet Control configuration

Instructions for configuring Raspberry Pi Zero as part of the QuadroFleet Control setup.

## 1. Update Raspberry Pi OS

1. Deactivate E3372 modem to change traffic routing. After reboot, it will be *automatically* restored.

   ```bash
   sudo nmcli connection show 
   ```
   
   ```bash
   sudo nmcli connection down "Wired connection 1" 
   ```

2. Ensure your Raspberry Pi OS (OS List 64-bit) is up-to-date:

   ```bash
   sudo apt update && sudo apt upgrade -y
   ```

## 2. Install GStreamer Libraries

Install necessary GStreamer libraries for media processing and plugin support:

```bash
sudo apt-get install -y \
    libgstreamer1.0-dev \
    libgstreamer-plugins-base1.0-dev \
    libgstreamer-plugins-bad1.0-dev \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
    gstreamer1.0-plugins-bad \
    gstreamer1.0-plugins-ugly \
    gstreamer1.0-libav \
    gstreamer1.0-tools \
    gstreamer1.0-x \
    gstreamer1.0-alsa \
    gstreamer1.0-gl \
    gstreamer1.0-gtk3 \
    gstreamer1.0-qt5 \
    gstreamer1.0-pulseaudio \
    libcamera-dev \
    gstreamer1.0-libcamera
```

## 3. Enable UART and Configure the Camera

Edit the Raspberry Pi configuration file to enable UART and configure the camera:

```bash
sudo nano /boot/firmware/config.txt
```

Add the following lines in the `[all]` section:

```ini
[all]
dtoverlay=ov5647,disable-bt
enable_uart=1
```

Save and exit by pressing `Ctrl + X`, then `Y`, and `Enter`.

## 4. Install OpenVPN

Set up OpenVPN by adding the repository and installing the OpenVPN 3 client.

1. **Add OpenVPN Keyring**:

   ```bash
   sudo mkdir -p /etc/apt/keyrings
   ```
   
   ```bash
   curl -fsSL https://packages.openvpn.net/packages-repo.gpg | sudo tee /etc/apt/keyrings/openvpn.asc
   ```

2. **Add OpenVPN Repository**:

   ```bash
   DISTRO=$(lsb_release -c -s)
   ```
   
   ```bash
   echo "deb [signed-by=/etc/apt/keyrings/openvpn.asc] https://packages.openvpn.net/openvpn3/debian $DISTRO main" | sudo tee /etc/apt/sources.list.d/openvpn-packages.list
   ```

3. **Update and Install OpenVPN**:

   ```bash
   sudo apt update && sudo apt install openvpn3 -y
   ```

4. **Enable Autoloading of OpenVPN**:

   1. Update `client.ovpn` file - insert content from downloaded profile.
   2. Update `client.autoload` file - set username and password: 

   ```json
   {
      "autostart": true,
      "user-auth": {
        "autologin": true,
        "username": "", // Set OpenVPN username
        "password": ""  // Set OpenVPN password
      }
   }
   ```

5. **Add Google DNS to `client.ovpn` in `/home/pi/.openvpn3/autoload` before key blocks**.

   ```ini
   dhcp-option DNS 8.8.8.8
   ```

6. **Deactivate DNS management of NetworkManager**:
   
   ```bash
   sudo nano /etc/NetworkManager/NetworkManager.conf
   ```

   **Add `dns=none` in `[main]` block:**
   ```ini
   [main]
   dns=none
   ```

7. **Update DNS resolver configuration**

   ```bash
   sudo nano /etc/resolv.conf
   ```
   
   Use only Google DNS. File content:

   ```ini
   nameserver 8.8.8.8
   ```

8. **Create and Enable OpenVPN Service**:

   Create the service file:

   ```bash
   sudo nano /etc/systemd/system/openvpn3.service
   ```

   Add the following content:

   ```ini
   [Unit]
   Description=OpenVPN 3 Linux configuration auto loader and starter
   After=network.target dbus.service
   StartLimitIntervalSec=30
   StartLimitBurst=10
   
   [Service]
   Type=simple
   ExecStart=/usr/sbin/openvpn3-autoload --directory /home/pi/.openvpn3/autoload
   Restart=always
   RestartSec=5
   User=pi
   
   [Install]
   WantedBy=multi-user.target
   ```

## 5. Set Up GStreamer Streaming Service

Create a systemd service for GStreamer to enable automatic streaming on boot.

1. **Create Service File**:

   ```bash
   sudo nano /etc/systemd/system/gstreamer-stream.service
   ```

2. **Service Configuration**:

   ```ini
   [Unit]
   Description=GStreamer Streaming Service
   After=network.target
   StartLimitIntervalSec=30
   StartLimitBurst=10

   [Service]
   ExecStart=/usr/bin/gst-launch-1.0 libcamerasrc ! video/x-raw,width=480,height=360,framerate=50/1 ! videoconvert ! x264enc bitrate=1000 speed-preset=ultrafast tune=zerolatency ! h264parse ! rtph264pay config-interval=1 pt=96 ! udpsink host=100.96.1.2 port=10900
   Restart=always
   RestartSec=5
   User=pi

   [Install]
   WantedBy=multi-user.target
   ```
   
3. **Replace target host ip `100.96.1.2` to correct one from OpenVPN subnet.**

   ```ini
   udpsink host=100.96.1.2
   ```

## 6. Set Up the RPi Zero Controller Service

Install SDKMan, Java, and configure the controller application.

### Install SDKMan

1. **Install SDKMan**:

   ```bash
   curl -s "https://get.sdkman.io" | bash
   ```
   
   ```bash
   source "$HOME/.sdkman/bin/sdkman-init.sh"
   ```

   ```bash
   sdk install java 21.0.5-amzn
   ```

   ### Prepare Files

   Copy `quadrofleet.jar` to `/home/pi/quadrofleet`.
   
   Create or update `env.properties`:
   
   ```ini
   udp.target.url=100.96.1.2
   udp.local.port=10800
   udp.target.port=10800
   udp.local.timeout=250
   udp.local.waiting=15000
   serial.port=/dev/serial0
   ```

   **Description**:
   - `udp.target.url=100.96.1.2`: Target host IP for UDP control streaming
   - `udp.local.port=10800`: Port for local UDP control messages
   - `udp.target.port=10800`: Target port for UDP control streaming
   - `udp.local.timeout=250`: Activate a timeout packet (armed + angle mode) after 250 ms
   - `udp.local.waiting=15000`: Send a timeout packet for 15 seconds
   - `serial.port=/dev/serial0`: Serial port interface connected to flight controller

## 7. Create and Enable Controller Service

1. **Create Controller Service File**:

   ```bash
   sudo nano /etc/systemd/system/quadrofleet-controller.service
   ```

2. **Add Service Configuration**:

   ```ini
   [Unit]
   Description=QuadroFleet Controller Service
   After=network.target
   StartLimitIntervalSec=30
   StartLimitBurst=10

   [Service]
   WorkingDirectory=/home/pi/quadrofleet
   ExecStart=/home/pi/.sdkman/candidates/java/current/bin/java -jar quadrofleet.jar
   Restart=always
   RestartSec=5
   User=pi

   [Install]
   WantedBy=multi-user.target
   ```

## 8. Enable all configured services

1. **Reload services**

   ```bash
   sudo systemctl daemon-reload
   ```

2. **OpenVPN3 service**

   ```bash
   sudo systemctl enable openvpn3.service
   ```
   
   ```bash
   sudo systemctl start openvpn3.service
   ```

3. **GStream service**

   ```bash
   sudo systemctl enable gstreamer-stream.service
   ```
   
   ```bash
   sudo systemctl start gstreamer-stream.service
   ```

4. **QuadroFleet service**

   ```bash
   sudo systemctl enable quadrofleet-controller.service
   ```
   
   ```bash
   sudo systemctl start quadrofleet-controller.service
   ```

## 9. Activate serial port

1. Run `raspi-config` configurator

   ```bash
   sudo raspi-config
   ```
2. Go to `3 Interface Options`
3. Go to `Serial Port`
4. Deactivate SSH via Serial Port `No`
5. Activate Serial port connections `Yes`
6. Exit & Reboot

## 9. Create an image from SD Card

1. Download and install [Win32 Disk Imager](https://win32diskimager.b-cdn.net/win32diskimager-1.0.0-install.exe)
2. Download and install [Paragon Linux File Systems for Windows](https://dl.paragon-software.com/demo/linuxwin7_trial.msi).
3. Use file manager and remove all sensitive data from SD Card
4. Create an image of SD Card
5. Use WSL and install PiShrink
   
   ```bash
   wget https://raw.githubusercontent.com/Drewsif/PiShrink/master/pishrink.sh
   ```
   
   ```bash
   chmod +x pishrink.sh
   ```
   
   ```bash
   sudo ./pishrink.sh sdcard.img sdcard_shrunk.img
   ```