#!/bin/bash

# Prompt for username of user. Default: pi
echo "Enter username (default: pi):"
read -p "Username: " OS_USERNAME
OS_USERNAME=${OS_USERNAME:-pi}  # Default to "pi" if no input
echo

# Prompt for OpenVPN username and password
echo "Enter OpenVPN credentials."
read -p "Username: " OPENVPN_USERNAME
read -s -p "Password: " OPENVPN_PASSWORD
echo

# Prompt for network settings with default ports
echo "Network settings."
read -p "Target IP (UDP Video Stream): " TARGET_IP # 100.96.1.2
read -p "Target port (UDP Video Stream) [default: 10900]: " TARGET_PORT
TARGET_PORT=${TARGET_PORT:-10900}  # Default to 10900 if no input

read -p "Local port (UDP Control Stream) [default: 10800]: " LOCAL_PORT
LOCAL_PORT=${LOCAL_PORT:-10800}  # Default to 10800 if no input
echo

# Update system
echo "Updating system..."
sudo apt update && sudo apt upgrade -y

# Install GStreamer Libraries
echo "Installing GStreamer Libraries..."
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

# Enable UART and configure the camera
echo "Configuring UART and Camera settings..."
sudo bash -c 'cat >> /boot/firmware/config.txt <<EOF
dtoverlay=ov5647,disable-bt
enable_uart=1
EOF'

# Install OpenVPN
echo "Installing OpenVPN 3..."
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://packages.openvpn.net/packages-repo.gpg | sudo tee /etc/apt/keyrings/openvpn.asc
DISTRO=$(lsb_release -c -s)
echo "deb [signed-by=/etc/apt/keyrings/openvpn.asc] https://packages.openvpn.net/openvpn3/debian $DISTRO main" | sudo tee /etc/apt/sources.list.d/openvpn-packages.list
sudo apt update && sudo apt install openvpn3 -y

# Create OpenVPN autoload files
echo "Creating OpenVPN autoload configuration..."
mkdir -p /home/$OS_USERNAME/.openvpn3/autoload
cat > /home/$OS_USERNAME/.openvpn3/autoload/client.autoload <<EOF
{
  "autostart": true,
  "user-auth": {
    "autologin": true,
    "username": "$OPENVPN_USERNAME",
    "password": "$OPENVPN_PASSWORD"
  }
}
EOF
chmod 600 /home/$OS_USERNAME/.openvpn3/autoload/client.autoload  # Secure file permissions

# Create OpenVPN service
echo "Creating OpenVPN systemd service..."
sudo bash -c "cat > /etc/systemd/system/openvpn3.service <<EOF
[Unit]
Description=OpenVPN 3 Linux configuration auto loader and starter
After=network.target dbus.service

[Service]
Type=simple
ExecStart=/usr/sbin/openvpn3-autoload --directory /home/$OS_USERNAME/.openvpn3/autoload
Restart=always
User=$OS_USERNAME

[Install]
WantedBy=multi-user.target
EOF"

sudo systemctl daemon-reload
sudo systemctl enable openvpn3.service
sudo systemctl start openvpn3.service

# Create GStreamer streaming service
echo "Creating GStreamer streaming systemd service..."
sudo bash -c "cat > /etc/systemd/system/gstreamer-stream.service <<EOF
[Unit]
Description=GStreamer Streaming Service
After=network.target

[Service]
ExecStart=/usr/bin/gst-launch-1.0 libcamerasrc ! video/x-raw,width=480,height=360,framerate=50/1 ! videoflip method=rotate-180 ! videoconvert ! x264enc bitrate=1000 speed-preset=ultrafast tune=zerolatency ! h264parse ! rtph264pay config-interval=1 pt=96 ! udpsink host=$TARGET_IP port=$TARGET_PORT
Restart=always
User=$OS_USERNAME

[Install]
WantedBy=multi-user.target
EOF"

sudo systemctl daemon-reload
sudo systemctl enable gstreamer-stream.service
sudo systemctl start gstreamer-stream.service

# Install SDKMan and Java
echo "Installing SDKMan and Java..."
curl -s "https://get.sdkman.io" | bash
source "/home/$OS_USERNAME/.sdkman/bin/sdkman-init.sh"  # Ensure SDKMan is sourced correctly
sdk install java 21.0.5-amzn

# Prepare QuadroFleet files
echo "Setting up QuadroFleet files..."
mkdir -p /home/$OS_USERNAME/quadrofleet
echo "Downloading quadrofleet.jar..."
curl -L -o /home/$OS_USERNAME/quadrofleet/quadrofleet.jar https://quadrofleet.com/files/quadrofleet.jar

# Create env.properties file with necessary configuration
cat > /home/$OS_USERNAME/quadrofleet/env.properties <<EOF
udp.local.port=$LOCAL_PORT
udp.target.url=$TARGET_IP
udp.target.port=$TARGET_PORT
serial.port=/dev/serial0
EOF

# Create QuadroFleet Controller service
echo "Creating QuadroFleet Controller systemd service..."
sudo bash -c "cat > /etc/systemd/system/quadrofleet-controller.service <<EOF
[Unit]
Description=QuadroFleet Controller Service
After=network.target

[Service]
WorkingDirectory=/home/$OS_USERNAME/quadrofleet
ExecStart=/home/$OS_USERNAME/.sdkman/candidates/java/current/bin/java -jar quadrofleet.jar
Restart=always
User=$OS_USERNAME

[Install]
WantedBy=multi-user.target
EOF"

sudo systemctl daemon-reload
sudo systemctl enable quadrofleet-controller.service
sudo systemctl start quadrofleet-controller.service

sudo raspi-config

echo "Setup completed. Check service statuses with 'sudo systemctl status <service_name>'."
