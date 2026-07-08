package com.quadrofleet;

import com.quadrofleet.helper.Utils;
import com.quadrofleet.service.FrameReceiverService;
import com.quadrofleet.service.FrameSenderService;
import com.quadrofleet.service.GamepadService;
import com.quadrofleet.service.OIPCReceiverService;
import com.quadrofleet.service.OSDService;
import com.quadrofleet.service.TrayIconService;
import com.quadrofleet.web.WebServerService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.freedesktop.gstreamer.Bin;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.State;
import org.freedesktop.gstreamer.Version;
import org.freedesktop.gstreamer.fx.FXImageSink;

public class App extends Application {

    private Pipeline pipeline;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        Utils.configurePaths();
        Gst.init(Version.BASELINE, "QuadroFleet");

        String pluginPath = System.getProperty("gstreamer.plugin.path");
        if (pluginPath != null && !pluginPath.isEmpty()) {
            org.freedesktop.gstreamer.Registry.get().scanPath(pluginPath);
        }
    }

    @Override
    public void start(Stage stage) {
        String appName = ConfigLoader.getInstance().getProperty("app.name");
        String gstreamerPipeline = ConfigLoader.getInstance().getProperty("gstreamer.pipeline");
        Integer gstreamerWidth = ConfigLoader.getInstance().getPropertyAsInteger("gstreamer.width");
        Integer gstreamerHeight = ConfigLoader.getInstance().getPropertyAsInteger("gstreamer.height");

        new GamepadService().start();
        new WebServerService().start();

        new FrameSenderService().start();
        new FrameReceiverService().start();
        new OIPCReceiverService().start();

        FXImageSink imageSink = new FXImageSink();
        imageSink.requestFrameSize(gstreamerWidth, gstreamerHeight);

        Bin bin = Gst.parseBinFromDescription(gstreamerPipeline, true);
        pipeline = new Pipeline();
        pipeline.add(bin);
        pipeline.add(imageSink.getSinkElement());
        bin.link(imageSink.getSinkElement());

        stage.setTitle(appName);

        BorderPane pane = new BorderPane();
        pane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        ImageView view = new ImageView();
        pane.setCenter(view);

        pane.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        new OSDService().setView(view).setPane(pane).start();

        view.imageProperty().bind(imageSink.imageProperty());
        view.fitWidthProperty().bind(pane.widthProperty());
        view.fitHeightProperty().bind(pane.heightProperty());
        view.setPreserveRatio(true);

        // Handle window close event
        stage.setOnCloseRequest(event -> {
            if (pipeline != null) {
                pipeline.setState(State.NULL);
                pipeline = null;
            }

            Platform.exit();
            System.exit(0);
        });

        stage.setScene(new Scene(pane, gstreamerWidth, gstreamerHeight));
        stage.show();
        pipeline.play();

        javax.swing.SwingUtilities.invokeLater(TrayIconService::initTrayIcon);
    }

}
