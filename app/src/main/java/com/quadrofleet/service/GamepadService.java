package com.quadrofleet.service;

import com.quadrofleet.helper.GamepadHelper;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import io.github.libsdl4j.api.event.SDL_Event;
import io.github.libsdl4j.api.event.events.SDL_JoyAxisEvent;
import io.github.libsdl4j.api.gamecontroller.SDL_GameController;
import io.github.libsdl4j.api.joystick.SDL_JoystickID;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static io.github.libsdl4j.api.Sdl.SDL_Init;
import static io.github.libsdl4j.api.Sdl.SDL_Quit;
import static io.github.libsdl4j.api.SdlSubSystemConst.SDL_INIT_EVERYTHING;
import static io.github.libsdl4j.api.error.SdlError.SDL_GetError;
import static io.github.libsdl4j.api.event.SDL_EventType.SDL_CONTROLLERDEVICEADDED;
import static io.github.libsdl4j.api.event.SDL_EventType.SDL_JOYAXISMOTION;
import static io.github.libsdl4j.api.event.SdlEvents.SDL_PollEvent;
import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.SDL_GameControllerOpen;
import static io.github.libsdl4j.api.hints.SdlHints.SDL_SetHint;

public class GamepadService {

    private final Logger logger = Logger.getLogger(GamepadService.class.getName());

    private final Thread THREAD;

    private JFrame overlay;

    private ScheduledExecutorService executor;

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

        // Overlay
        // Set up overlay
        setupOverlay();

        // Initialize the scheduler
        executor = Executors.newScheduledThreadPool(1);

        // Schedule the overlay update at a 20ms interval
        executor.scheduleAtFixedRate(() -> {
            WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, "GStreamer D3D video sink (internal window)");

            if (hwnd != null) {
                WinDef.RECT rect = new WinDef.RECT();
                User32.INSTANCE.GetWindowRect(hwnd, rect);

                SwingUtilities.invokeLater(() -> {
                    if (!overlay.isVisible()) {
                        overlay.setVisible(true); // Show overlay only once when window is found
                    }
                    overlay.setSize(rect.right - rect.left, rect.bottom - rect.top);
                    overlay.setLocation(rect.left, rect.top);
                });
            } else if (overlay.isVisible()) {
                SwingUtilities.invokeLater(() -> overlay.setVisible(false)); // Hide overlay if window disappears
            }
        }, 1000, 20, TimeUnit.MILLISECONDS); // Initial delay of 100ms

        while (ConfigService.getInstance().isRunSDLEventLoop()) {

            if (sdlGameController == null) {
                sdlGameController = SDL_GameControllerOpen(0);
            }

            while (SDL_PollEvent(evt) != 0) {

                updateGamepadButtons(sdlGameController, evt.jaxis);

                switch (evt.type) {
                    case SDL_CONTROLLERDEVICEADDED:
                        System.out.println("Controller added: " + evt.cdevice);
                        break;
                    case SDL_JOYAXISMOTION:
                        if (evt.jaxis.which.equals(new SDL_JoystickID(0))) {
                            updateGamepadSticks(evt.jaxis);
                        }
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

    private void updateGamepadButtons(SDL_GameController sdlGameController, SDL_JoyAxisEvent event) {
        if (GamepadHelper.isPressedUp(sdlGameController)) {
            FlightConfigService.getInstance()
                    .getFlightConfig()
                    .resetThrottle();
            FlightConfigService.getInstance()
                    .getFlightConfig()
                    .setArmed(true);
        }

        if (GamepadHelper.isPressedDown(sdlGameController)) {
            FlightConfigService.getInstance()
                    .getFlightConfig()
                    .setArmed(false);
        }

        if (GamepadHelper.isPressedRight(sdlGameController)) {
            FlightConfigService.getInstance()
                    .getFlightConfig()
                    .setAngleMode(true);
        }

        if (GamepadHelper.isPressedLeft(sdlGameController)) {
            FlightConfigService.getInstance()
                    .getFlightConfig()
                    .setAngleMode(false);
        }

        if (GamepadHelper.isPressedRB(sdlGameController) && GamepadHelper.isPressedA(sdlGameController)) {
            FlightConfigService.getInstance()
                    .getFlightConfig()
                    .setAltHoldMode(true);
        }

        if (GamepadHelper.isPressedRB(sdlGameController) && GamepadHelper.isPressedB(sdlGameController)) {
            FlightConfigService.getInstance()
                    .getFlightConfig()
                    .setAltHoldMode(false);
        }

        FlightConfigService
                .getInstance()
                .getFlightStatus()
                .setArmed(FlightConfigService.getInstance()
                        .getFlightConfig()
                        .isArmed());

        FlightConfigService
                .getInstance()
                .getFlightStatus()
                .setAngleMode(FlightConfigService.getInstance()
                        .getFlightConfig()
                        .isAngleMode());

        FlightConfigService
                .getInstance()
                .getFlightStatus()
                .setAltHoldMode(FlightConfigService.getInstance()
                        .getFlightConfig()
                        .isAltHoldMode());
    }

    private void updateGamepadSticks(SDL_JoyAxisEvent event) {
        if (event.axis == 0) {
            // Left Stick X Axis
            FlightConfigService.getInstance().getFlightConfig().setRoll(GamepadHelper.convertShortToDouble(event.value));
        } else if (event.axis == 1) {
            // Left Stick Y Axis
            FlightConfigService.getInstance().getFlightConfig().setPitch(GamepadHelper.convertShortToDouble(event.value));
        } else if (event.axis == 2) {
            // Right Stick X Axis
            FlightConfigService.getInstance().getFlightConfig().setYaw(GamepadHelper.convertShortToDouble(event.value));
        } else if (event.axis == 3) {
            // Right Stick Y Axis
            FlightConfigService.getInstance().getFlightConfig().setThrottle(GamepadHelper.convertShortToDouble(event.value));
        }
    }

    private void setupOverlay() {
        overlay = new JFrame();
        overlay.setUndecorated(true);
        overlay.setBackground(new Color(0, 0, 0, 0)); // Transparent background
        overlay.setAlwaysOnTop(true);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.RED);
                g.drawString("Overlay Text", 150, 150);
            }
        };
        panel.setOpaque(false);
        overlay.add(panel);
        overlay.setVisible(true);
    }

}
