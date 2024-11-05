package com.quadrofleet.gamepad;

import com.quadrofleet.service.ConfigService;
import io.github.libsdl4j.api.event.SDL_Event;
import io.github.libsdl4j.api.gamecontroller.SDL_GameController;
import io.github.libsdl4j.api.gamecontroller.SDL_GameControllerButton;

import javax.swing.*;

import static io.github.libsdl4j.api.Sdl.SDL_Init;
import static io.github.libsdl4j.api.Sdl.SDL_Quit;
import static io.github.libsdl4j.api.SdlSubSystemConst.SDL_INIT_EVERYTHING;
import static io.github.libsdl4j.api.error.SdlError.SDL_GetError;
import static io.github.libsdl4j.api.event.SDL_EventType.SDL_CONTROLLERDEVICEADDED;
import static io.github.libsdl4j.api.event.SdlEvents.SDL_PollEvent;
import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.SDL_GameControllerGetButton;
import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.SDL_GameControllerOpen;
import static io.github.libsdl4j.api.hints.SdlHints.SDL_SetHint;

public class GamepadService {

    private final Thread THREAD;

    public GamepadService() {
        THREAD = new Thread(this::SDLInitialization);
    }

    public void start() {
        THREAD.start();
    }

    private void SDLInitialization() {
        int result = SDL_Init(SDL_INIT_EVERYTHING);

        if (result != 0) {
            JOptionPane.showMessageDialog(
                    null,
                    "Unable to initialize SDL library (Error code " + result + "): " + SDL_GetError(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        // Allow background joystick actions
        SDL_SetHint("SDL_JOYSTICK_ALLOW_BACKGROUND_EVENTS", "1");

        SDL_GameController sdlGameController = null;
        SDL_Event evt = new SDL_Event();

        while (ConfigService.getInstance().isRunSDLEventLoop()) {

            if (sdlGameController == null) {
                sdlGameController = SDL_GameControllerOpen(0);
            }

            while (SDL_PollEvent(evt) != 0) {

                //
                boolean gamepadAButtonPressed = SDL_GameControllerGetButton(sdlGameController, SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_A) == 1;

                if (gamepadAButtonPressed) {
                    ConfigService.getInstance().setStatus(true);
                }

                System.out.println("GameController A: " + gamepadAButtonPressed);
                //

                //
                boolean gamepadBButtonPressed = SDL_GameControllerGetButton(sdlGameController, SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_B) == 1;

                if (gamepadBButtonPressed) {
                    ConfigService.getInstance().setRunSDLEventLoop(false);
                }

                System.out.println("GameController B: " + gamepadAButtonPressed);
                //

                switch (evt.type) {
                    case SDL_CONTROLLERDEVICEADDED:
                        System.out.println("Controller added: " + evt.cdevice);
                        break;
                    default:
                        break;
                }
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        SDL_Quit();
    }

}
