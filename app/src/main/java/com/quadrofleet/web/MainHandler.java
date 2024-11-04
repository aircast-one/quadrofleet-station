package com.quadrofleet.web;

import com.quadrofleet.service.ConfigService;
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
        ByteBuffer content = ByteBuffer.wrap(Files.readAllBytes(Paths.get("src/main/resources/index.html")));
        response.getHeaders().put(HttpHeader.CONTENT_LENGTH, content.remaining());

        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/html");
        response.getHeaders().put(HttpHeader.CONTENT_LENGTH, content.remaining());
        response.setStatus(HttpStatus.OK_200);
        response.write(true, content, callback);

        callback.succeeded();
        return true;
    }

    private static boolean getStatus(Response response, Callback callback) {
        ByteBuffer content = ByteBuffer.wrap(
                ("{ \"status\": \"" + ConfigService.getInstance().getStatus() + "\"}").getBytes(StandardCharsets.UTF_8));

        response.getHeaders().put(HttpHeader.CONTENT_LENGTH, content.remaining());

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
            case "/status" -> getStatus(response, callback);

            default -> notFound(response, callback);
        };
    }

}
