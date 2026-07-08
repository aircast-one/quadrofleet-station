#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# package-mac.sh - Build a self-contained macOS .app / .dmg for QuadroFleet
# Station with a bundled JRE, a pruned GStreamer runtime, and SDL2. The result
# runs with no external dependencies (no Homebrew, no GStreamer.framework).
#
# Usage:
#   ./scripts/package-mac.sh <numeric-version>     # e.g. 1.0.0
#
# Optional signing / notarization (skipped when unset):
#   MACOS_SIGNING_IDENTITY   - Developer ID Application identity
#   MACOS_NOTARIZE_PROFILE   - notarytool keychain profile name
#
# Source of native libraries (override for CI):
#   GSTREAMER_FRAMEWORK      - default /Library/Frameworks/GStreamer.framework
#   SDL2_DYLIB               - default from Homebrew prefix
# =============================================================================

VERSION="${1:?Usage: $0 <numeric-version>}"
APP_NAME="QuadroFleet Station"
MAIN_JAR="app.jar"
MAIN_CLASS="com.quadrofleet.Launcher"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
GST_FW="${GSTREAMER_FRAMEWORK:-/Library/Frameworks/GStreamer.framework}"
SDL2_DYLIB="${SDL2_DYLIB:-$(brew --prefix 2>/dev/null)/lib/libSDL2.dylib}"

GST_SRC="$GST_FW/Versions/Current/lib"
# Plugins required by the app pipeline (application.properties):
#   udpsrc rtph265depay h265parse avdec_h265 videoscale videoconvert appsink
# In GStreamer 1.24 videoscale/videoconvert are provided by videoconvertscale.
PLUGINS=(
  libgstcoreelements.dylib
  libgstudp.dylib
  libgstrtp.dylib
  libgstrtpmanager.dylib
  libgstvideoparsersbad.dylib
  libgstlibav.dylib
  libgstvideoconvertscale.dylib
  libgstapp.dylib
  libgsttypefindfunctions.dylib
  libgstplayback.dylib
  libgstautodetect.dylib
)

BUILD="$ROOT/build"
STAGE="$BUILD/pkg-mac"
INPUT="$STAGE/input"
DEST="$BUILD/dist-mac"

echo "▶ Building runtime jars"
cd "$ROOT"
./gradlew --console=plain -q clean installDist

echo "▶ Staging application jar (fat jar with all dependencies)"
rm -rf "$STAGE" "$DEST"
mkdir -p "$INPUT" "$DEST"
cp "$ROOT/app/build/install/app/lib/app.jar" "$INPUT/"

echo "▶ Bundling pruned GStreamer runtime"
# Flat layout: GStreamer is relocatable and finds its plugins at
# <dir-of-libgstreamer>/gstreamer-1.0, so no framework/symlink structure is
# needed (jpackage does not preserve symlinks in --input anyway).
GST_DST="$INPUT/native/gstreamer"
mkdir -p "$GST_DST/gstreamer-1.0"
# core shared libraries (self-contained closure for the plugins above)
cp "$GST_SRC"/*.dylib "$GST_DST/"
# only the plugins we use
for p in "${PLUGINS[@]}"; do
  if [ -f "$GST_SRC/gstreamer-1.0/$p" ]; then
    cp "$GST_SRC/gstreamer-1.0/$p" "$GST_DST/gstreamer-1.0/"
  else
    echo "  ! plugin not found, skipping: $p" >&2
  fi
done

echo "▶ Bundling SDL2"
mkdir -p "$INPUT/native/sdl2"
cp -L "$SDL2_DYLIB" "$INPUT/native/sdl2/libSDL2.dylib"

echo "▶ Bundle size: $(du -sh "$INPUT/native" | cut -f1)"

SIGN_ARGS=()
if [ -n "${MACOS_SIGNING_IDENTITY:-}" ]; then
  SIGN_ARGS=(--mac-sign --mac-signing-key-user-name "$MACOS_SIGNING_IDENTITY")
  echo "▶ Signing enabled: $MACOS_SIGNING_IDENTITY"
fi

echo "▶ jpackage ($1)"
jpackage \
  --type dmg \
  --name "$APP_NAME" \
  --app-version "$VERSION" \
  --input "$INPUT" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --java-options "-Dgstreamer.path=\$APPDIR/native/gstreamer" \
  --java-options "-Dsdl2.path=\$APPDIR/native/sdl2" \
  --java-options "-Djava.library.path=\$APPDIR/native/sdl2" \
  --dest "$DEST" \
  "${SIGN_ARGS[@]}"

DMG="$(ls "$DEST"/*.dmg)"
echo "▶ Built: $DMG"

if [ -n "${MACOS_NOTARIZE_PROFILE:-}" ]; then
  echo "▶ Notarizing"
  xcrun notarytool submit "$DMG" --keychain-profile "$MACOS_NOTARIZE_PROFILE" --wait
  xcrun stapler staple "$DMG"
fi

echo "▶ Done: $DMG"
