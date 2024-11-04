package com.quadrofleet.gamepad;

import com.quadrofleet.service.ConfigService;
import io.github.libsdl4j.api.event.SDL_Event;
import io.github.libsdl4j.api.gamecontroller.SDL_GameController;
import io.github.libsdl4j.api.gamecontroller.SDL_GameControllerButton;
import io.github.libsdl4j.api.video.SDL_Window;
import io.github.libsdl4j.api.video.SDL_WindowFlags;

import static io.github.libsdl4j.api.Sdl.SDL_Init;
import static io.github.libsdl4j.api.Sdl.SDL_Quit;
import static io.github.libsdl4j.api.SdlSubSystemConst.SDL_INIT_EVERYTHING;
import static io.github.libsdl4j.api.error.SdlError.SDL_GetError;
import static io.github.libsdl4j.api.event.SDL_EventType.SDL_CONTROLLERDEVICEADDED;
import static io.github.libsdl4j.api.event.SDL_EventType.SDL_KEYDOWN;
import static io.github.libsdl4j.api.event.SDL_EventType.SDL_QUIT;
import static io.github.libsdl4j.api.event.SDL_EventType.SDL_WINDOWEVENT;
import static io.github.libsdl4j.api.event.SdlEvents.SDL_PollEvent;
import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.SDL_GameControllerGetButton;
import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.SDL_GameControllerOpen;
import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.SDL_GameControllerUpdate;
import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.SDL_IsGameController;
import static io.github.libsdl4j.api.hints.SdlHints.SDL_SetHint;
import static io.github.libsdl4j.api.keycode.SDL_Keycode.SDLK_SPACE;
import static io.github.libsdl4j.api.video.SdlVideo.SDL_CreateWindow;
import static io.github.libsdl4j.api.video.SdlVideoConst.SDL_WINDOWPOS_CENTERED;

public class GamepadService {

    private final Thread THREAD;

    public GamepadService() {
        THREAD = new Thread(this::SDLInitialization);
    }

    public void start() {
        THREAD.start();
    }

    private void SDLInitialization() {
        // Initialize SDL
        int result = SDL_Init(SDL_INIT_EVERYTHING);

        //

        SDL_GameControllerUpdate();

        SDL_GameController sdlGameController = SDL_GameControllerOpen(0);

        SDL_SetHint("SDL_JOYSTICK_ALLOW_BACKGROUND_EVENTS", "1");

        boolean isJoystick = SDL_IsGameController(0);
        System.out.println("Is Gamepad " + isJoystick);

        //

        if (result != 0) {
            throw new IllegalStateException("Unable to initialize SDL library (Error code " + result + "): " + SDL_GetError());
        }

        // Create and init the window
        SDL_Window window = SDL_CreateWindow("Demo SDL2", SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, 50, 50, SDL_WindowFlags.SDL_WINDOW_HIDDEN);

        if (window == null) {
            throw new IllegalStateException("Unable to create SDL window: " + SDL_GetError());
        }

        // Start an event loop and react to events
        SDL_Event evt = new SDL_Event();
        boolean shouldRun = true;
        while (shouldRun) {
            while (SDL_PollEvent(evt) != 0) {

                boolean gamepadAButtonPressed = SDL_GameControllerGetButton(sdlGameController, SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_A) == 1;

                if (gamepadAButtonPressed) {
                    ConfigService.getInstance().setStatus(true);
                }

                System.out.println("Gamecontroller A: " + gamepadAButtonPressed);

                switch (evt.type) {
                    case SDL_QUIT:
                        shouldRun = false;
                        break;
                    case SDL_KEYDOWN:
                        if (evt.key.keysym.sym == SDLK_SPACE) {
                            System.out.println("SPACE pressed");
                        }
                        break;
                    case SDL_WINDOWEVENT:
                        System.out.println("Window event " + evt.window.event);
                        break;
                    case SDL_CONTROLLERDEVICEADDED:
                        System.out.println("Controller added: " + evt.cdevice);
                        break;
                    default:
                        break;
                }
            }
        }

        SDL_Quit();
    }

}
