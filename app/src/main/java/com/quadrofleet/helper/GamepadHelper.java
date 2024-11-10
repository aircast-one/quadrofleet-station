package com.quadrofleet.helper;

import com.quadrofleet.service.FlightConfigService;
import io.github.libsdl4j.api.event.events.SDL_JoyAxisEvent;
import io.github.libsdl4j.api.gamecontroller.SDL_GameController;
import io.github.libsdl4j.api.gamecontroller.SDL_GameControllerButton;

import java.util.Locale;
import java.util.stream.Collectors;

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

    public static String drawTarget(double heading, String homeSymbol, String targetSymbol) {
        String[] result = new String[21];

        for (int i = 0; i < 21; i++) {
            result[i] = "&nbsp";
        }

        FlightConfigService.getInstance().getFlightConfig().getMapPoints().forEach(point -> {
            if (point.isTarget()) {
                long targetHeading = GamepadHelper.calculateAngle(
                        FlightConfigService.getInstance().getFlightStatus().getLatitude(),
                        FlightConfigService.getInstance().getFlightStatus().getLongitude(),
                        point.getLatitude(),
                        point.getLongitude()
                );

                int position = offsetPos(heading, targetHeading);

                if (position >= 0) {
                    result[position] = targetSymbol;
                }
            } else {
                long homeHeading = GamepadHelper.calculateAngle(
                        FlightConfigService.getInstance().getFlightStatus().getLatitude(),
                        FlightConfigService.getInstance().getFlightStatus().getLongitude(),
                        point.getLatitude(),
                        point.getLongitude()
                );

                int position = offsetPos(heading, homeHeading);

                if (position >= 0) {
                    result[position] = homeSymbol;
                }
            }
        });

        return String.join("", result);
    }

    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371e3 * c;
    }

    public static String generateTargetDistanceList(double windowHeightParam) {
        return FlightConfigService.getInstance().getFlightConfig().getMapPoints().stream().map(point -> {
            double longitude = FlightConfigService.getInstance().getFlightStatus().getLongitude();
            double latitude = FlightConfigService.getInstance().getFlightStatus().getLatitude();

            String icon = (point.isHome()) ? getIcon("e88a", (int) (windowHeightParam * 8)) : getIcon("e55d", (int) (windowHeightParam * 8));

            return "<p>" + icon + " " + String.format(Locale.US, "%dm", Math.round(haversineDistance(
                    latitude,
                    longitude,
                    point.getLatitude(),
                    point.getLongitude()
            ))) + "</p>";
        }).collect(Collectors.joining());
    }

    public static String getIcon(String code, int size) {
        return "<span style=\"font-family: Material Icons; font-size: " + size + "px\">&#x" + code + "</span>";
    }

}
