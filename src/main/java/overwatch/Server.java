package overwatch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.iki.elonen.NanoHTTPD;
import overwatch.dto.InitDto;
import overwatch.service.ConfigurationService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class Server extends NanoHTTPD {

    private static final Logger logger = Logger.getLogger(Server.class.getName());

    @Override
    public Response serve(IHTTPSession session) {
        if(session.getHeaders().getOrDefault("key", "").equals(ConfigurationService.getString(ConfigurationService.Keys.OVERWATCH_INIT_KEY)))
            return createError(401, "Wrong key");

        if(session.getMethod() == Method.POST)
            return serveInit(session);

        if (session.getMethod() == Method.GET)
            return serveImage(session);

        return createError(405, "Method Not Allowed");
    }

    private Response serveInit(IHTTPSession session){
        Optional<InitDto> requestOptional = readInitRequestFromSession(session);
        if(requestOptional.isEmpty())
            return createError(409, "Empty data");
        InitDto request = requestOptional.get();
        Engine.start(request.toZones());
        return createSuccessful();
    }

    private Response serveImage(IHTTPSession session){
        return createError(409, "Not implemented");
    }

    private static Optional<InitDto> readInitRequestFromSession(IHTTPSession session){
        return readPayloadFromSession(session)
                .flatMap(payload -> {
                   ObjectMapper mapper = new ObjectMapper();
                    try {
                        return Optional.of(mapper.readValue(payload, InitDto.class));
                    } catch (JsonProcessingException e) {
                        logger.severe(e.getMessage());
                        return Optional.empty();
                    }
                });
    }

    private static Optional<String> readPayloadFromSession(IHTTPSession session) {
        Map<String, String> map = new HashMap<>();
        try {
            session.parseBody(map);
        }
        catch (Exception e){
            logger.severe(e.getMessage());
        }
        return Optional.ofNullable(map.get("postData"));
    }

    private static Response createError(int status, String message){
        return newFixedLengthResponse(new HTTPStatus(status), NanoHTTPD.MIME_PLAINTEXT, message);
    }

    private static Response createSuccessful(){
        return newFixedLengthResponse(new HTTPStatus(200), NanoHTTPD.MIME_PLAINTEXT, "");
    }

    public Server() {
        super(ConfigurationService.getInt(ConfigurationService.Keys.SERVER_PORT));
    }

    @Override
    public void start() throws IOException {
        this.start(-1, false);
        logger.info("Server is running");
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

