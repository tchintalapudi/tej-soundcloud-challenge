package com.followermaze;

import com.followermaze.handler.EventHandler;
import com.followermaze.handler.ClientHandler;

import java.io.IOException;

public interface ServerRegistry {
	void registerClient(ClientHandler clientHandler);

	void registerEventSource(EventHandler eventHandler);

	void handleEvent(String event, int eventCounter) throws IOException;
}
