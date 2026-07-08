package com.quadrofleet.helper;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;

import java.io.File;
import java.util.Objects;
import java.util.stream.Stream;

public class Utils {

    private Utils() {
    }

    /**
     * Configures paths to the native libraries (GStreamer and SDL2). On Windows
     * queries various GStreamer environment variables, and then sets up the PATH
     * environment variable. On macOS, adds the locations to jna.library.path
     * (macOS binaries link to each other): the GStreamer framework plus the
     * Homebrew library directories where {@code brew install sdl2} places
     * libSDL2. On both, the gstreamer.path system property can be used to
     * override GStreamer, and sdl2.path can override the SDL2 location. On Linux,
     * assumes both are in the path already.
     */
    public static void configurePaths() {
        if (Platform.isWindows()) {
            String gstPath = System.getProperty("gstreamer.path", findWindowsLocation());
            if (!gstPath.isEmpty()) {
                String systemPath = System.getenv("PATH");
                if (systemPath == null || systemPath.trim().isEmpty()) {
                    Kernel32.INSTANCE.SetEnvironmentVariable("PATH", gstPath);
                } else {
                    Kernel32.INSTANCE.SetEnvironmentVariable("PATH", gstPath
                            + File.pathSeparator + systemPath);
                }
            }
        } else if (Platform.isMac()) {
            appendJnaLibraryPath(System.getProperty("gstreamer.path",
                    "/Library/Frameworks/GStreamer.framework/Libraries/"));

            String sdlPath = System.getProperty("sdl2.path");
            if (sdlPath != null) {
                appendJnaLibraryPath(sdlPath);
            } else {
                appendJnaLibraryPath("/opt/homebrew/lib");
                appendJnaLibraryPath("/usr/local/lib");
            }
        } else {
            appendJnaLibraryPath(System.getProperty("gstreamer.path"));
            appendJnaLibraryPath(System.getProperty("sdl2.path"));
        }
    }

    /**
     * Appends a single directory to the jna.library.path system property,
     * preserving any value already present.
     *
     * @param path directory to add; ignored when null or empty
     */
    private static void appendJnaLibraryPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return;
        }

        String current = System.getProperty("jna.library.path", "").trim();
        if (current.isEmpty()) {
            System.setProperty("jna.library.path", path);
        } else {
            System.setProperty("jna.library.path", current + File.pathSeparator + path);
        }
    }

    /**
     * Query over a stream of possible environment variables for GStreamer
     * location, filtering on the first non-null result, and adding \bin\ to the
     * value.
     *
     * @return location or empty string
     */
    public static String findWindowsLocation() {
        if (Platform.is64Bit()) {
            return Stream.of("GSTREAMER_1_0_ROOT_MSVC_X86_64",
                            "GSTREAMER_1_0_ROOT_MINGW_X86_64",
                            "GSTREAMER_1_0_ROOT_X86_64")
                    .map(System::getenv)
                    .filter(Objects::nonNull)
                    .map(p -> p.endsWith("\\") ? p + "bin\\" : p + "\\bin\\")
                    .findFirst().orElse("");
        } else {
            return "";
        }
    }

}
