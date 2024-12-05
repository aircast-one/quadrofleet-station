# QuadroFleet Control Configuration

**Instructions for configuring a Raspberry Pi Zero 2 W as part of the QuadroFleet Control setup.**

---

## 1. Update Raspberry Pi OS

1. Deactivate the E3372 modem to change traffic routing. After reboot, it will be *automatically* restored:

   ```bash
   sudo nmcli connection show
   sudo nmcli connection down "Wired connection 1"
   ```

2. Ensure Raspberry Pi OS (64-bit) is up to date:

   ```bash
   sudo apt update && sudo apt upgrade -y
   ```

---

## 2. Install GStreamer Libraries

Install the required GStreamer libraries for media processing and plugin support:

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

---

## 3. Enable UART and Configure the Camera

1. Edit the Raspberry Pi configuration file:

   ```bash
   sudo nano /boot/firmware/config.txt
   ```

2. Add the following lines to the `[all]` section:

   ```ini
   [all]
   dtoverlay=ov5647,disable-bt
   enable_uart=1
   ```

3. Save and exit (`Ctrl + X`, then `Y`, and `Enter`).

---

## 4. Install OpenVPN

1. Add the OpenVPN Keyring:

   ```bash
   sudo mkdir -p /etc/apt/keyrings
   curl -fsSL https://packages.openvpn.net/packages-repo.gpg | sudo tee /etc/apt/keyrings/openvpn.asc
   ```

2. Add the OpenVPN Repository:

   ```bash
   DISTRO=$(lsb_release -c -s)
   echo "deb [signed-by=/etc/apt/keyrings/openvpn.asc] https://packages.openvpn.net/openvpn3/debian $DISTRO main" | sudo tee /etc/apt/sources.list.d/openvpn-packages.list
   ```

3. Update and Install OpenVPN:

   ```bash
   sudo apt update && sudo apt install openvpn3 -y
   ```

4. Configure OpenVPN:

   - Add the `client.ovpn` file from your profile to `/home/pi/.openvpn3/autoload`.
   - Add Google DNS to the `client.ovpn` file:

     ```ini
     dhcp-option DNS 8.8.8.8
     ```
     
   - Modify `client.autoload` with your credentials:

     ```json
     {
       "autostart": true,
       "user-auth": {
         "autologin": true,
         "username": "YOUR_USERNAME",
         "password": "YOUR_PASSWORD"
       }
     }
     ```

5. Disable NetworkManager's DNS management:

   Edit the NetworkManager configuration:

   ```bash
   sudo nano /etc/NetworkManager/NetworkManager.conf
   ```

   Add:

   ```ini
   [main]
   dns=none
   ```

   Update `/etc/resolv.conf` to use Google DNS *only*:

   ```ini
   nameserver 8.8.8.8
   ```

6. **Create and Enable the OpenVPN Service:**

   ```bash
   sudo nano /etc/systemd/system/openvpn3.service
   ```

   Add:

   ```ini
   [Unit]
   Description=OpenVPN 3 Service
   After=network.target dbus.service

   [Service]
   Type=simple
   ExecStart=/usr/sbin/openvpn3-autoload --directory /home/pi/.openvpn3/autoload
   Restart=always
   RestartSec=5
   User=pi

   [Install]
   WantedBy=multi-user.target
   ```

---

## 5. Configure GStreamer Streaming

1. **Create a GStreamer Service:**

   ```bash
   sudo nano /etc/systemd/system/gstreamer-stream.service
   ```

2. **Add the following configuration:**

   ```ini
   [Unit]
   Description=GStreamer Streaming Service
   After=network.target

   [Service]
   ExecStart=/usr/bin/gst-launch-1.0 libcamerasrc ! video/x-raw,width=480,height=360,framerate=50/1 ! videoconvert ! x264enc bitrate=1000 speed-preset=ultrafast tune=zerolatency ! h264parse ! rtph264pay config-interval=1 pt=96 ! udpsink host=100.96.1.2 port=10900
   Restart=always
   RestartSec=5
   User=pi

   [Install]
   WantedBy=multi-user.target
   ```

   Replace `100.96.1.2` with the correct host IP from your OpenVPN subnet.

---

## 6. Install Java and Configure Controller

1. **Install SDKMan and Java:**

   ```bash
   curl -s "https://get.sdkman.io" | bash
   source "$HOME/.sdkman/bin/sdkman-init.sh"
   sdk install java 21.0.5-amzn
   ```

2. **Prepare Files:**

   - Copy `quadrofleet.jar` to `/home/pi/quadrofleet`.
   - Create `env.properties`:

     ```ini
     udp.target.url=100.96.1.2
     udp.local.port=10800
     udp.target.port=10800
     udp.local.timeout=250
     udp.local.waiting=15000
     serial.port=/dev/serial0
     ```

   Replace `100.96.1.2` with the correct host IP from your OpenVPN subnet.

   - `udp.local.port=10800`: UDP port for receiving control messages.
   - `udp.target.port=10800`: UDP port for sending telemetry messages to the remote target (QuadroFleet Station).
   - `udp.local.timeout=250`: Time (in ms) to wait for a control packet before sending a default timeout packet to stabilize the drone (armed, angle mode).
   - `udp.local.waiting=15000`: Duration (in ms) to send timeout packets before activating failsafe mode.
   - `serial.port=/dev/serial0`: Serial port interface connected to the flight controller.

---

## 7. Enable and Start Services

1. Reload systemd:

   ```bash
   sudo systemctl daemon-reload
   ```

2. Enable and start each service:

   - **OpenVPN:**

     ```bash
     sudo systemctl enable openvpn3.service
     sudo systemctl start openvpn3.service
     ```

   - **GStreamer:**

     ```bash
     sudo systemctl enable gstreamer-stream.service
     sudo systemctl start gstreamer-stream.service
     ```

   - **QuadroFleet Controller:**

     ```bash
     sudo systemctl enable quadrofleet-controller.service
     sudo systemctl start quadrofleet-controller.service
     ```

---

## 8. Activate Serial Port

1. Run `raspi-config`:

   ```bash
   sudo raspi-config
   ```

2. Navigate to `3 Interface Options` → `Serial Port`:
   - Disable shell access over serial (`No`).
   - Enable serial port (`Yes`).

3. Exit and reboot.

---

## 9. Create SD Card Image

1. Prepare Tools:
   - Install [Win32 Disk Imager](https://win32diskimager.b-cdn.net/win32diskimager-1.0.0-install.exe).
   - Install [Paragon Linux File Systems for Windows](https://dl.paragon-software.com/demo/linuxwin7_trial.msi).

2. Create an Image:
   - Use `Win32 Disk Imager`

3. Shrink Image (reduce image size):
   - Use `PiShrink`:

     ```bash
     wget https://raw.githubusercontent.com/Drewsif/PiShrink/master/pishrink.sh
     chmod +x pishrink.sh
     sudo ./pishrink.sh sdcard.img sdcard_shrunk.img
     ```
