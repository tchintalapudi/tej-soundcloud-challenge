package com.followermaze;

import com.followermaze.handler.ClientHandler;
import com.followermaze.handler.EventHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Basic tests for some socket server.
 */
public class SocketServerTest {

    SocketServer socketServer;
    int eventCounter;


    private static List<String> createEvents() {
        List<String> events = new ArrayList<>();
        events.add("666|F|60|50");
        events.add("1|U|12|9");
        events.add("542532|B");
        events.add("634|S|32");
        return events;
    }

    @Before
    public void setUp() throws Exception {
        socketServer = new  SocketServer();
        eventCounter = 0;
    }

    @Test
    public void testRegisterClient() throws IOException {
        Socket socketMock = mock(Socket.class);
        OutputStream istreamMock = mock(OutputStream.class);
        InputStream ostreamMock = mock(InputStream.class);
        when(socketMock.getOutputStream()).thenReturn(istreamMock);
        when(socketMock.getInputStream()).thenReturn(ostreamMock);
        ClientHandler clientHandler = new ClientHandler(socketMock, socketServer);
        Assert.assertNotNull(clientHandler.getClientSocket());
    }

    @Test
    public void testRegisterEvent() throws IOException {
        EventHandler eventHandlerMock = mock(EventHandler.class);
        socketServer.registerEventSource(eventHandlerMock);
        Assert.assertNotNull(socketServer.eventHandler);
    }


    @Test
    public void testHandleEventSceanrio1() throws IOException {
        Map<Integer, ClientHandler> clientConnections = new HashMap<>();
        ClientHandler fromUserMock = mock(ClientHandler.class);
        ClientHandler toUserMock = mock(ClientHandler.class);
        Set<ClientHandler> followers = new HashSet<>();
        followers.add(toUserMock);
        when(fromUserMock.getFollowers()).thenReturn(followers);
        clientConnections.put(12, fromUserMock);
        clientConnections.put(9, toUserMock);
        socketServer.setClientConnections(clientConnections);
        Assert.assertTrue(!fromUserMock.getFollowers().isEmpty());
        for (String event :
                createEvents()) {
            socketServer.handleEvent(event, eventCounter++);
        }
        Assert.assertTrue(fromUserMock.getFollowers().isEmpty());
    }

    @Test
    public void testHandleEventScenario2() throws IOException {
        Map<Integer, ClientHandler> clientConnections = new HashMap<>();
        ClientHandler fromUserMock = mock(ClientHandler.class);
        ClientHandler toUserMock = mock(ClientHandler.class);
        clientConnections.put(60, fromUserMock);
        clientConnections.put(50, toUserMock);
        socketServer.setClientConnections(clientConnections);
        for (String event :
                createEvents()) {
            socketServer.handleEvent(event, eventCounter++);
        }
        Assert.assertTrue(fromUserMock.getFollowers().isEmpty());
    }

}
