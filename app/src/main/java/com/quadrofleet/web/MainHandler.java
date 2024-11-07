package com.quadrofleet.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quadrofleet.service.FlightConfigService;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainHandler extends Handler.Abstract {

    private static boolean indexPage(Response response, Callback callback) throws IOException {
        ByteBuffer content = ByteBuffer.wrap(Files.readAllBytes(Paths.get("src/main/resources/web/map.html")));

        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/html");
        response.getHeaders().put(HttpHeader.CONTENT_LENGTH, content.remaining());
        response.setStatus(HttpStatus.OK_200);
        response.write(true, content, callback);

        callback.succeeded();
        return true;
    }

    private static boolean getFavicon(Response response, Callback callback) throws IOException {
        ByteBuffer content = ByteBuffer.wrap(Files.readAllBytes(Paths.get("src/main/resources/web/favicon.ico")));

        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "image/x-icon");
        response.getHeaders().put(HttpHeader.CONTENT_LENGTH, content.remaining());
        response.setStatus(HttpStatus.OK_200);
        response.write(true, content, callback);

        callback.succeeded();
        return true;
    }

    private static boolean getPointerPng(Response response, Callback callback) throws IOException {
        ByteBuffer content = ByteBuffer.wrap(Files.readAllBytes(Paths.get("src/main/resources/web/img/pointer.png")));

        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "image/png");
        response.getHeaders().put(HttpHeader.CONTENT_LENGTH, content.remaining());
        response.setStatus(HttpStatus.OK_200);
        response.write(true, content, callback);

        callback.succeeded();
        return true;
    }

    private static boolean styleCss(Response response, Callback callback) throws IOException {
        ByteBuffer content = ByteBuffer.wrap(Files.readAllBytes(Paths.get("src/main/resources/web/css/style.css")));

        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/css");
        response.getHeaders().put(HttpHeader.CONTENT_LENGTH, content.remaining());
        response.setStatus(HttpStatus.OK_200);
        response.write(true, content, callback);

        callback.succeeded();
        return true;
    }

    private static boolean jsScript(Response response, Callback callback) throws IOException {
        ByteBuffer content = ByteBuffer.wrap(Files.readAllBytes(Paths.get("src/main/resources/web/js/script.js")));

        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/javascript");
        response.getHeaders().put(HttpHeader.CONTENT_LENGTH, content.remaining());
        response.setStatus(HttpStatus.OK_200);
        response.write(true, content, callback);

        callback.succeeded();
        return true;
    }

    private static boolean getTelemetry(Response response, Callback callback) {

        String data;

        try {
            data = FlightConfigService.getInstance().getJSONFlightStatus();
        } catch (JsonProcessingException e) {
            data = "JsonProcessingException: " + e.getMessage();
        }

        ByteBuffer content = ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8));

        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "application/json");
        response.getHeaders().put(HttpHeader.CONTENT_LENGTH, content.remaining());
        response.setStatus(HttpStatus.OK_200);
        response.write(true, content, callback);

        callback.succeeded();
        return true;
    }

    private static boolean notFound(Response response, Callback callback) {
        response.setStatus(HttpStatus.NOT_FOUND_404);
        callback.succeeded();
        return false;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        return switch (request.getHttpURI().getPath()) {

            case "/" -> indexPage(response, callback);
            case "/telemetry" -> getTelemetry(response, callback);

            case "/img/favicon.ico" -> getFavicon(response, callback);
            case "/img/pointer.png" -> getPointerPng(response, callback);
            case "/css/style.css" -> styleCss(response, callback);
            case "/js/script.js" -> jsScript(response, callback);

            default -> notFound(response, callback);
        };
    }

}
