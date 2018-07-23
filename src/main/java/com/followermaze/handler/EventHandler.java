package com.followermaze.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import com.followermaze.ServerRegistry;
import org.apache.log4j.Logger;

/**
 * Event handler thread which handles the processing of the events.
 */
public class EventHandler extends Thread {

	static Logger logger = Logger.getLogger(EventHandler.class);

	private ServerSocket eventSocket;
	private BufferedReader eventIn;
	private int eventCounter;
	ServerRegistry callback;

	public ServerSocket getEventSocket() {
		return eventSocket;
	}

	public EventHandler(ServerRegistry callback) {
		super();
		this.callback = callback;
	}

	public void openEventSocket(int eventListenerPort) {
		try {
			eventSocket = new ServerSocket(eventListenerPort);
			logger.info("listening for events on port " + eventListenerPort);
			Socket eventConnection = eventSocket.accept();
			eventIn = new BufferedReader(new InputStreamReader(eventConnection.getInputStream()));
			logger.info("connected eventstream on " + eventListenerPort);
			callback.registerEventSource(this);
			eventCounter = 0;
		} catch (Exception e) {
			logger.error("Error opening socket ", e);
		}
	}

	@Override
	public void run() {
		try {
			readEvents();
		} finally {
			try {
				eventSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				eventIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void readEvents() {
		String event;
		try {
			while ((event = eventIn.readLine()) != null) {
				eventCounter++;
				callback.handleEvent(event, eventCounter);
			}
		} catch (IOException e) {
			logger.error("Error reading stream", e);
		}
	}
}
