package overwatch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.iki.elonen.NanoHTTPD;
import overwatch.dto.InitDto;
import overwatch.service.ConfigurationService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class Server extends NanoHTTPD {

    private static final Logger logger = Logger.getLogger(Server.class.getName());

    @Override
    public Response serve(IHTTPSession session) {
        if(!session.getHeaders().getOrDefault("key", "").equals(ConfigurationService.getString(ConfigurationService.Keys.OVERWATCH_KEY)))
            return createError(Response.Status.UNAUTHORIZED, "Wrong key");

        if(session.getMethod() == Method.POST)
            return serveInit(session);

        if (session.getMethod() == Method.GET)
            return serveImage();

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

    private Response serveImage() {
        final BufferedImage image = Engine.getGeneratedImage();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, "JPEG",output);
        }
        catch (IOException e) {
            return createError(Response.Status.INTERNAL_ERROR, "Error during image serving.");
        }
        final ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

        Response response = newFixedLengthResponse(Response.Status.OK, "/image/jpeg", input, output.size());
        response.addHeader("content-type", "image/jpeg");
        return response;
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

