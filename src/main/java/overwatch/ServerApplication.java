package overwatch;

import overwatch.service.ConfigurationService;

import java.io.IOException;

public class ServerApplication {

    public static void main (String[] args) {
        ConfigurationService.override(ConfigurationService.Keys.DEBUG_FRAME_ENABLE, "false");
        Server server = new Server();
        try {
            server.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
