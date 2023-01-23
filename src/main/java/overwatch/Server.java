package overwatch;

import fi.iki.elonen.NanoHTTPD;
import overwatch.service.ConfigurationService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class Server extends NanoHTTPD {
    @Override
    public Response serve(IHTTPSession session) {
        if (session.getMethod() != Method.POST)
            return createError(405, "Method Not Allowed");
        String payload = readFromInputStream(session.getInputStream());
        System.out.print(payload);
        return createSuccessful();
    }
    private Response createError(int status, String message){
        return newFixedLengthResponse(new HTTPStatus(status), NanoHTTPD.MIME_PLAINTEXT, message);
    }
    private String readFromInputStream(InputStream stream){
        String text = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining());
        return text;
    }

    private Response createSuccessful(){
        return newFixedLengthResponse(new HTTPStatus(200), NanoHTTPD.MIME_PLAINTEXT, "");
    }

    public Server() {
        super(ConfigurationService.getInt(ConfigurationService.Keys.SERVER_PORT));
    }
    private static class HTTPStatus implements Response.IStatus {
        private final int statusCode;

        private HTTPStatus(int statusCode) {
            this.statusCode = statusCode;
        }

        @Override
        public String getDescription() {
            return "";
        }

        @Override
        public int getRequestStatus() {
            return statusCode;
        }
    }
}

