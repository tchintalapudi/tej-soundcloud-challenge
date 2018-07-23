package com.followermaze;

import com.followermaze.handler.ClientHandler;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Application starts the event handler and starts listening for
 * clients
 *
 */
public class Application {

    static Logger logger = Logger.getLogger(Application.class);


    public static void main(String[] args) {
        SocketServer server = new SocketServer();
        server.startEventConnection();
        try {
            server.listenForConnectingClients();
        } catch (IOException e) {
            logger.error(" Error getting client connections", e);
        } finally {
            try {
                server.eventHandler.getEventSocket().close();
                for (ClientHandler uct : server.getClientConnections()
                        .values()) {
                    uct.getClientSocket().close();
                }
            } catch (IOException e) {
                logger.error("Error closing sockets", e);
            }
        }
    }
}
