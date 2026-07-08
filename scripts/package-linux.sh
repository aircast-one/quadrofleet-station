#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# package-linux.sh - Build a self-contained Linux .deb for QuadroFleet Station.
#
# Usage:
#   ./scripts/package-linux.sh <numeric-version>     # e.g. 1.0.0
#
# Bundles a JRE plus the GStreamer runtime and SDL2 so the app runs with no
# external dependencies. Distro shared objects have no usable RUNPATH, so each
# bundled .so is patched with an $ORIGIN rpath to find its siblings, and the
# app registers the bundled plugin directory at startup (distro GStreamer is
# not relocatable). Requires patchelf.
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

LIB_DST="$INPUT/native/gstreamer/lib"
PLUGIN_DST="$LIB_DST/gstreamer-1.0"
SDL_DST="$INPUT/native/sdl2"

PLUGINS=(
  libgstcoreelements.so libgstudp.so libgstrtp.so libgstrtpmanager.so
  libgstvideoparsersbad.so libgstlibav.so libgstvideoconvertscale.so
  libgstvideoscale.so libgstvideoconvert.so
  libgstapp.so libgsttypefindfunctions.so libgstplayback.so libgstautodetect.so
)

command -v patchelf >/dev/null || { echo "patchelf is required" >&2; exit 1; }

echo "▶ Building runtime jars"
cd "$ROOT"
./gradlew --console=plain -q clean installDist

echo "▶ Staging"
rm -rf "$STAGE" "$DEST"; mkdir -p "$PLUGIN_DST" "$SDL_DST" "$DEST"
cp "$ROOT/app/build/install/app/lib/app.jar" "$INPUT/"

echo "▶ Bundling GStreamer + dependency closure"
collect_deps() { ldd "$1" 2>/dev/null | awk '/=> \//{print $3}'; }
declare -A SEEN
copy_closure() {
  local lib="$1"
  [ -z "$lib" ] && return
  local base; base=$(basename "$lib")
  [ -n "${SEEN[$base]:-}" ] && return
  SEEN[$base]=1
  case "$lib" in
    /lib/*|*/libc.so*|*/libm.so*|*/libpthread.so*|*/libdl.so*|*/ld-linux*) return ;; # keep core glibc from host
  esac
  cp -L "$lib" "$LIB_DST/" 2>/dev/null || return
  local dep; for dep in $(collect_deps "$lib"); do copy_closure "$dep"; done
}
cp -L "$GST_LIBDIR/libgstreamer-1.0.so.0" "$LIB_DST/"
for dep in $(collect_deps "$GST_LIBDIR/libgstreamer-1.0.so.0"); do copy_closure "$dep"; done
for p in "${PLUGINS[@]}"; do
  src="$GST_PLUGINDIR/$p"
  [ -f "$src" ] || { echo "  ! skip $p"; continue; }
  cp -L "$src" "$PLUGIN_DST/"
  for dep in $(collect_deps "$src"); do copy_closure "$dep"; done
done

echo "▶ Bundling SDL2"
SDL2_SO="$(ldconfig -p | awk '/libSDL2-2.0.so.0/{print $NF; exit}')"
cp -L "$SDL2_SO" "$SDL_DST/libSDL2.so"
for dep in $(collect_deps "$SDL2_SO"); do copy_closure "$dep"; done

echo "▶ Patching rpaths to \$ORIGIN (relocatable bundle)"
for so in "$LIB_DST"/*.so*; do patchelf --set-rpath '$ORIGIN' "$so"; done
for so in "$PLUGIN_DST"/*.so*; do patchelf --set-rpath '$ORIGIN/..' "$so"; done
patchelf --set-rpath '$ORIGIN' "$SDL_DST/libSDL2.so"

echo "▶ jpackage"
jpackage \
  --type deb \
  --name "$APP_NAME" \
  --app-version "$VERSION" \
  --input "$INPUT" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --java-options "-Dgstreamer.path=\$APPDIR/native/gstreamer/lib" \
  --java-options "-Dgstreamer.plugin.path=\$APPDIR/native/gstreamer/lib/gstreamer-1.0" \
  --java-options "-Dsdl2.path=\$APPDIR/native/sdl2" \
  --java-options "-Djna.library.path=\$APPDIR/native/gstreamer/lib:\$APPDIR/native/sdl2" \
  --linux-shortcut \
  --dest "$DEST"

echo "▶ Done: $(ls "$DEST"/*.deb)"
