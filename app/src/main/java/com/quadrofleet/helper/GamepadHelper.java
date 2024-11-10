package com.quadrofleet.helper;

import io.github.libsdl4j.api.event.events.SDL_JoyAxisEvent;
import io.github.libsdl4j.api.gamecontroller.SDL_GameController;
import io.github.libsdl4j.api.gamecontroller.SDL_GameControllerButton;

import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.SDL_GameControllerGetButton;

public class GamepadHelper {

    private static final double DEGTORAD = Math.PI / 180.0;

    private static final double RADTODEG = 180.0 / Math.PI;

    private GamepadHelper() {
        //
    }

    public static boolean isPressedA(SDL_GameController sdlGameController) {
        return SDL_GameControllerGetButton(sdlGameController, SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_A) == 1;
    }

    public static boolean isPressedB(SDL_GameController sdlGameController) {
        return SDL_GameControllerGetButton(sdlGameController, SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_B) == 1;
    }

    public static boolean isPressedRT(SDL_JoyAxisEvent event) {
        return event.axis == 5 && event.value > -30_000;
    }

    public static boolean isPressedLT(SDL_JoyAxisEvent event) {
        return event.axis == 4 && event.value > -30_000;
    }

    public static boolean isPressedRB(SDL_GameController sdlGameController) {
        return SDL_GameControllerGetButton(sdlGameController, SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_RIGHTSHOULDER) == 1;
    }

    public static boolean isPressedLB(SDL_GameController sdlGameController) {
        return SDL_GameControllerGetButton(sdlGameController, SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_LEFTSHOULDER) == 1;
    }

    public static boolean isPressedUp(SDL_GameController sdlGameController) {
        return SDL_GameControllerGetButton(sdlGameController, SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_DPAD_UP) == 1;
    }

    public static boolean isPressedDown(SDL_GameController sdlGameController) {
        return SDL_GameControllerGetButton(sdlGameController, SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_DPAD_DOWN) == 1;
    }

    public static boolean isPressedLeft(SDL_GameController sdlGameController) {
        return SDL_GameControllerGetButton(sdlGameController, SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_DPAD_LEFT) == 1;
    }

    public static boolean isPressedRight(SDL_GameController sdlGameController) {
        return SDL_GameControllerGetButton(sdlGameController, SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_DPAD_RIGHT) == 1;
    }

    public static double convertShortToDouble(short value) {
        return value / 32767.5;
    }

    public static long calculateAngle(double lat1, double lon1, double lat2, double lon2) {
        if (lat2 == 0 && lon2 == 0) {
            return 0;
        }

        double dLon = (lon2 - lon1) * DEGTORAD;
        double y = Math.sin(dLon) * Math.cos(lat2 * DEGTORAD);
        double x = Math.cos(lat1 * DEGTORAD) * Math.sin(lat2 * DEGTORAD) - Math.sin(lat1 * DEGTORAD) * Math.cos(lat2 * DEGTORAD) * Math.cos(dLon);
        double angle = Math.atan2(y, x) * RADTODEG;

        return Math.round(angle);
    }

    public static double calculateOffset(double heading, double target) {
        double result = ((target - heading + 180) % 360) - 180;

        return (result > -180) ? result : result + 360;
    }

    public static int offsetPos(double heading, double target) {
        double offset = calculateOffset(heading, target);

        if (offset < -90 || offset > 90) {
            return -1;
        }

        return (int) ((offset + 90) * 20 / 180);
    }

    public static String drawTarget(double heading, double home, String homeSymbol, double target, String targetSymbol) {
        int targetPos = offsetPos(heading, target);
        int homePos = offsetPos(heading, home);

        String[] result = new String[21];

        for (int i = 0; i < 21; i++) {
            if (target != 0 && targetPos == i) {
                result[i] = targetSymbol;
            } else if (home != 0 && homePos == i) {
                result[i] = homeSymbol;
            } else {
                result[i] = "&nbsp";
            }
        }

        return String.join("", result);
    }

}
