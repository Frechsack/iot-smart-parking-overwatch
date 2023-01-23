package overwatch;

import java.io.IOException;

public class Application {

    public static void main (String[] args) {
        Server server = new Server();
        try {
            server.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
