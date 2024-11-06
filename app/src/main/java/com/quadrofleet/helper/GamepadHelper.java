package com.quadrofleet.helper;

import io.github.libsdl4j.api.event.events.SDL_JoyAxisEvent;
import io.github.libsdl4j.api.gamecontroller.SDL_GameController;
import io.github.libsdl4j.api.gamecontroller.SDL_GameControllerButton;

import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.SDL_GameControllerGetButton;

public class GamepadHelper {

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

}
