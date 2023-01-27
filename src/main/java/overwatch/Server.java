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
        if(!session.getHeaders().getOrDefault("key", "").equals(ConfigurationService.getString(ConfigurationService.Keys.OVERWATCH_INIT_KEY)))
            return createError(Response.Status.UNAUTHORIZED, "Wrong key");

        if(session.getMethod() == Method.POST)
            return serveInit(session);

        if (session.getMethod() == Method.GET)
            return serveImage(session);

        return createError(Response.Status.METHOD_NOT_ALLOWED, "Method Not Allowed");
    }

    private Response serveInit(IHTTPSession session){
        Optional<InitDto> requestOptional = readInitRequestFromSession(session);
        if(requestOptional.isEmpty())
            return createError(Response.Status.CONFLICT, "Empty data");
        InitDto request = requestOptional.get();
        Engine.start(request.toZones());
        return createSuccessful();
    }

    private Response serveImage(IHTTPSession session){
        return createError(Response.Status.CONFLICT, "Not implemented");
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

    private static Response createError(Response.IStatus status, String message){
        logger.warning("HttpError: status: '" + status +  "', message: '" + message + "'");
        return newFixedLengthResponse(status, NanoHTTPD.MIME_PLAINTEXT, message);
    }

    private static Response createSuccessful(){
        return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "");
    }

    public Server() {
        super(ConfigurationService.getInt(ConfigurationService.Keys.SERVER_PORT));
    }

    @Override
    public void start() throws IOException {
        this.start(-1, false);
        logger.info("Server is running");
    }
}

