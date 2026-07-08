# Running QuadroFleet Station on macOS

Instructions for building and running the QuadroFleet ground station on macOS
(Apple Silicon and Intel).

## 1. Prerequisites

Install a Java 21 JDK, GStreamer, and SDL2. [Homebrew](https://brew.sh) is the
simplest route:

```bash
brew install openjdk@21 gstreamer sdl2
```

GStreamer can also be installed from the official
[macOS packages](https://gstreamer.freedesktop.org/download/#macos) (installed to
`/Library/Frameworks/GStreamer.framework`). Install both the **Runtime** and
**Development** packages so the H.265 decoder (`avdec_h265`, provided by
`gst-libav`) is available.

The application looks for native libraries in this order:

- **GStreamer**: `/Library/Frameworks/GStreamer.framework/Libraries/`
- **SDL2** (gamepad support): `/opt/homebrew/lib` (Apple Silicon) and
  `/usr/local/lib` (Intel), where Homebrew places `libSDL2.dylib`

Override either location with `-Dgstreamer.path=...` or `-Dsdl2.path=...` if you
installed them elsewhere.

## 2. Build and run

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew run
```

The first run downloads the Gradle distribution and project dependencies.

## 3. Configuration

Runtime settings live in `app/src/main/resources/application.properties`:

- `server.port` — port for the built-in map server (default `8090`). Change it if
  the port is already in use.
- `gstreamer.pipeline` — the GStreamer pipeline used to receive and decode the
  video stream.

## Notes

- The gamepad is read through SDL2; connect an Xbox or PlayStation controller
  before launching, or hot-plug it while the app is running.
- Apple Silicon is fully supported. The app avoids GStreamer's variadic
  `gst_bin_add_many` / `gst_element_link_many` bindings, which are not callable
  through JNA on `arm64`.
