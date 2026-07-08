#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# package-linux.sh - Build a self-contained Linux .deb for QuadroFleet Station.
#
# Usage:
#   ./scripts/package-linux.sh <numeric-version>     # e.g. 1.0.0
#
# NOTE: This follows the proven macOS packaging pattern but has NOT yet been
# validated end-to-end. Distro GStreamer libraries are not relocatable, so the
# generated launcher exports GST_PLUGIN_SYSTEM_PATH_1_0 / LD_LIBRARY_PATH to the
# bundled runtime. Validate on a CI runner before relying on it.
# =============================================================================

VERSION="${1:?Usage: $0 <numeric-version>}"
APP_NAME="quadrofleet-station"
MAIN_JAR="app.jar"
MAIN_CLASS="com.quadrofleet.Launcher"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ARCH=$(dpkg-architecture -qDEB_HOST_MULTIARCH 2>/dev/null || echo x86_64-linux-gnu)
GST_LIBDIR="${GST_LIBDIR:-/usr/lib/$ARCH}"
GST_PLUGINDIR="${GST_PLUGINDIR:-$GST_LIBDIR/gstreamer-1.0}"

BUILD="$ROOT/build"
STAGE="$BUILD/pkg-linux"
INPUT="$STAGE/input"
DEST="$BUILD/dist-linux"

PLUGINS=(
  libgstcoreelements.so libgstudp.so libgstrtp.so libgstrtpmanager.so
  libgstvideoparsersbad.so libgstlibav.so libgstvideoconvertscale.so
  libgstvideoscale.so libgstvideoconvert.so
  libgstapp.so libgsttypefindfunctions.so libgstplayback.so libgstautodetect.so
)

echo "▶ Building runtime jars"
cd "$ROOT"
./gradlew --console=plain -q clean installDist

echo "▶ Staging"
rm -rf "$STAGE" "$DEST"; mkdir -p "$INPUT/native/gstreamer/gstreamer-1.0" "$INPUT/native/sdl2" "$DEST"
cp "$ROOT/app/build/install/app/lib/app.jar" "$INPUT/"

echo "▶ Bundling GStreamer + dependency closure"
# copy the plugins we use plus the transitive shared-library closure via ldd
collect_deps() {
  ldd "$1" 2>/dev/null | awk '/=> \//{print $3}'
}
declare -A SEEN
copy_closure() {
  local lib="$1"
  [ -z "$lib" ] && return
  local base; base=$(basename "$lib")
  [ -n "${SEEN[$base]:-}" ] && return
  SEEN[$base]=1
  case "$lib" in
    /lib/*|/usr/lib/x86_64-linux-gnu/libc.so*|*/ld-linux*) return ;; # keep glibc from host
  esac
  cp -L "$lib" "$INPUT/native/gstreamer/" 2>/dev/null || return
  local dep; for dep in $(collect_deps "$lib"); do copy_closure "$dep"; done
}
for p in "${PLUGINS[@]}"; do
  src="$GST_PLUGINDIR/$p"
  [ -f "$src" ] || { echo "  ! skip $p"; continue; }
  cp -L "$src" "$INPUT/native/gstreamer/gstreamer-1.0/"
  for dep in $(collect_deps "$src"); do copy_closure "$dep"; done
done
cp -L "$GST_LIBDIR/libgstreamer-1.0.so.0" "$INPUT/native/gstreamer/" 2>/dev/null || true

echo "▶ Bundling SDL2"
cp -L "$(ldconfig -p | awk '/libSDL2-2.0.so.0/{print $NF; exit}')" "$INPUT/native/sdl2/" 2>/dev/null || true

echo "▶ jpackage"
jpackage \
  --type deb \
  --name "$APP_NAME" \
  --app-version "$VERSION" \
  --input "$INPUT" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --java-options "-Dgstreamer.path=\$APPDIR/native/gstreamer" \
  --java-options "-Dsdl2.path=\$APPDIR/native/sdl2" \
  --java-options "-Djna.library.path=\$APPDIR/native/gstreamer:\$APPDIR/native/sdl2" \
  --linux-shortcut \
  --dest "$DEST"

echo "▶ Done: $(ls "$DEST"/*.deb)"
