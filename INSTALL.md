# quadrofleet-station
QuadroFleet Station Control

## RPI

sudo apt update
sudo apt upgrade

sudo apt-get install libgstreamer1.0-dev libgstreamer-plugins-base1.0-dev libgstreamer-plugins-bad1.0-dev gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-bad gstreamer1.0-plugins-ugly gstreamer1.0-libav gstreamer1.0-tools gstreamer1.0-x gstreamer1.0-alsa gstreamer1.0-gl gstreamer1.0-gtk3 gstreamer1.0-qt5 gstreamer1.0-pulseaudio libcamera-dev gstreamer1.0-libcamera

gst-launch-1.0 libcamerasrc ! video/x-raw,width=640,height=480,framerate=60/1 ! videoflip method=rotate-180 ! videoconvert ! x264enc bitrate=1000 speed-preset=ultrafast tune=zerolatency ! h264parse ! rtph264pay config-interval=1 pt=96 ! udpsink host=192.168.178.38 port=2222

sudo nano /boot/firmware/config.txt

	```
	[all]
	dtoverlay=ov5647,disable-bt
	enable_uart=1
	```

## PC

-No frame info
gst-launch-1.0.exe udpsrc port=2222 ! application/x-rtp,encoding-name=H264,payload=96 ! rtph264depay ! avdec_h264 ! autovideosink sync=false

-With frame info
gst-launch-1.0.exe udpsrc port=2222 ! application/x-rtp,encoding-name=H264,payload=96 ! rtph264depay ! avdec_h264 ! fpsdisplaysink sync=false

-Frame info with scaled font (change the resolution to your own)
gst-launch-1.0.exe udpsrc port=2222 ! application/x-rtp,encoding-name=H264,payload=96 ! rtph264depay ! avdec_h264 ! videoscale ! video/x-raw,width=1280,height=960 ! fpsdisplaysink sync=false

## OpenVPN

sudo mkdir -p /etc/apt/keyrings && curl -fsSL https://packages.openvpn.net/packages-repo.gpg | sudo tee /etc/apt/keyrings/openvpn.asc

DISTRO=$(lsb_release -c -s)

echo "deb [signed-by=/etc/apt/keyrings/openvpn.asc] https://packages.openvpn.net/openvpn3/debian $DISTRO main" | sudo tee /etc/apt/sources.list.d/openvpn-packages.list

sudo apt update

sudo apt install openvpn3

## OpenVPN3 DOCS

https://openvpn.net/cloud-docs/owner/settings/settings---users/set-the-default-connection-authentication--connect-auth--policy-for-users.html

https://blog.openvpn.net/openvpn-3-linux-and-auth-user-pass/

## RPI Boot Stream

To run your GStreamer pipeline as a daemon on Raspberry Pi OS Lite 64-bit, you can create a custom systemd service to automatically start it in the background on boot. Here’s a step-by-step guide to set it up:

1. **Create a systemd service file:**

   Open a new file in `/etc/systemd/system` with a descriptive name, for example, `gstreamer-stream.service`:

   ```bash
   sudo nano /etc/systemd/system/gstreamer-stream.service
   ```

2. **Add the following configuration to the service file:**

   ```ini
   [Unit]
   Description=GStreamer Streaming Service
   After=network.target

   [Service]
   ExecStart=/usr/bin/gst-launch-1.0 libcamerasrc ! video/x-raw,width=640,height=480,framerate=50/1 ! videoflip method=rotate-180 ! videoconvert ! x264enc bitrate=1000 speed-preset=ultrafast tune=zerolatency ! h264parse ! rtph264pay config-interval=1 pt=96 ! udpsink host=100.96.1.2 port=2222
   Restart=always
   User=pi   # Change to your username if not 'pi'

   [Install]
   WantedBy=multi-user.target
   ```

    - **ExecStart:** The command to start the GStreamer pipeline.
    - **Restart=always:** Ensures that the service restarts automatically if it fails.
    - **User=pi:** Sets the user to run the command as (change to your user if different).

3. **Save and close the file:**

   Press `Ctrl + X`, then `Y`, and `Enter` to save and exit.

4. **Reload systemd to recognize the new service:**

   ```bash
   sudo systemctl daemon-reload
   ```

5. **Enable the service to start at boot:**

   ```bash
   sudo systemctl enable gstreamer-stream.service
   ```

6. **Start the service immediately (for testing):**

   ```bash
   sudo systemctl start gstreamer-stream.service
   ```

7. **Check the status to ensure it’s running correctly:**

   ```bash
   sudo systemctl status gstreamer-stream.service
   ```

## RPIZ Controller

SAME AND:

[Unit]
Description=RPi Zero Controller Service
After=network.target

[Service]
WorkingDirectory=/home/sam/rpiz
ExecStart=/home/sam/.sdkman/candidates/java/current/bin/java -jar rpiz.jar
Restart=always
User=sam

[Install]
WantedBy=multi-user.target