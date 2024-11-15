# QuadroFleet Station Control

Instructions for configuring Raspberry Pi Zero as part of the QuadroFleet Station Control setup.

## 1. Update Raspberry Pi OS

Ensure your Raspberry Pi OS (OS List 64-bit) is up-to-date:

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
   curl -fsSL https://packages.openvpn.net/packages-repo.gpg | sudo tee /etc/apt/keyrings/openvpn.asc
   ```

2. **Add OpenVPN Repository**:

   ```bash
   DISTRO=$(lsb_release -c -s)
   echo "deb [signed-by=/etc/apt/keyrings/openvpn.asc] https://packages.openvpn.net/openvpn3/debian $DISTRO main" | sudo tee /etc/apt/sources.list.d/openvpn-packages.list
   ```

3. **Update and Install OpenVPN**:

   ```bash
   sudo apt update && sudo apt install openvpn3 -y
   ```

4. **Enable Autoloading of OpenVPN**:

   Create `client.autoload` in `/home/pi/.openvpn3/autoload`.

   ```bash
   mkdir -p /home/pi/.openvpn3/autoload
   ```

   **client.autoload format**:
   ```json
   {
      "autostart": true,
      "user-auth": {
        "autologin": true,
        "username": "", // Username to OpenVPN
        "password": ""  // Password to OpenVPN
      }
   }
   ```

   **Set Permissions**:
   Secure `client.autoload` with restricted permissions:
   ```bash
   chmod 600 /home/pi/.openvpn3/autoload/client.autoload
   ```

5. **Create and Enable OpenVPN Service**:

   Create the service file:

   ```bash
   sudo nano /etc/systemd/system/openvpn3.service
   ```

   Add the following content:

   ```ini
   [Unit]
   Description=OpenVPN 3 Linux configuration auto loader and starter
   After=network.target dbus.service
   
   [Service]
   Type=oneshot
   ExecStart=/usr/sbin/openvpn3-autoload --directory /home/pi/.openvpn3/autoload
   Restart=always
   User=pi
   
   [Install]
   WantedBy=multi-user.target
   ```

   Enable and start the service:

   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable openvpn3.service
   sudo systemctl start openvpn3.service
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

   [Service]
   ExecStart=/usr/bin/gst-launch-1.0 libcamerasrc ! video/x-raw,width=640,height=480,framerate=50/1 ! videoflip method=rotate-180 ! videoconvert ! x264enc bitrate=0 speed-preset=ultrafast tune=zerolatency ! h264parse ! rtph264pay config-interval=1 pt=96 ! udpsink host=100.96.1.2 port=2222
   Restart=always
   User=pi

   [Install]
   WantedBy=multi-user.target
   ```

3. **Reload Systemd and Enable Service**:

   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable gstreamer-stream.service
   sudo systemctl start gstreamer-stream.service
   ```

4. **Check Service Status**:

   ```bash
   sudo systemctl status gstreamer-stream.service
   ```

## 6. Set Up the RPi Zero Controller Service

Install SDKMan, Java, and configure the controller application.

### Install SDKMan

1. **Install SDKMan**:

   ```bash
   curl -s "https://get.sdkman.io" | bash
   source "$HOME/.sdkman/bin/sdkman-init.sh"  # Ensure SDKMan is sourced correctly
   sdk version  # Verify installation
   ```

2. **Install Required Java Version**:

   ```bash
   sdk install java 21.0.5-amzn
   ```

### Prepare Files

Copy `quadrofleet.jar` to `/home/pi/quadrofleet`.

Create or update `env.properties`:

```bash
udp.local.port=10800
udp.target.url=100.96.1.2
udp.target.port=10800
serial.port=/dev/serial0
```

**Description**:
- `udp.local.port=10800`: Port for local UDP control messages
- `udp.target.url=100.96.1.2`: Target host IP for UDP control streaming
- `udp.target.port=10800`: Target port for UDP control streaming
- `serial.port=/dev/serial0`: Serial port connected to flight controller

### Create and Enable Controller Service

1. **Create Controller Service File**:

   ```bash
   sudo nano /etc/systemd/system/quadrofleet-controller.service
   ```

2. **Add Service Configuration**:

   ```ini
   [Unit]
   Description=QuadroFleet Controller Service
   After=network.target

   [Service]
   WorkingDirectory=/home/pi/quadrofleet
   ExecStart=/home/pi/.sdkman/candidates/java/current/bin/java -jar quadrofleet.jar
   Restart=always
   User=pi

   [Install]
   WantedBy=multi-user.target
   ```

3. **Reload Systemd and Enable Service**:

   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable quadrofleet-controller.service
   sudo systemctl start quadrofleet-controller.service
   ```

4. **Check Service Status**:

   ```bash
   sudo systemctl status quadrofleet-controller.service
   ```

---

### Additional Notes:
- **Verify Permissions**: Ensure proper ownership and permissions for sensitive configuration files, especially for OpenVPN credentials.
- **SDKMan Environment**: Double-check that the SDKMan installation initializes correctly to avoid Java path issues.