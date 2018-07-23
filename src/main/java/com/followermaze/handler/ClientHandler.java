package com.followermaze.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;
import java.util.TreeSet;

import com.followermaze.ServerRegistry;
import org.apache.log4j.Logger;

/**
 * Client handler thread. Each client subscribing to the source will get a thread.
 * Each client registers with the socket server.
 */
public class ClientHandler extends Thread  implements  Comparable<ClientHandler> {

	private Integer clientId;

	private Socket clientSocket;

	private Integer eventCounter = 0;

	private BufferedReader eventIn;

	private PipedOutputStream eventStream;
	private Set<ClientHandler> followers = new TreeSet<ClientHandler>();
	private BufferedReader socketIn;

	private PrintWriter socketOut;

	ServerRegistry socketServer;

	static Logger logger = Logger.getLogger(ClientHandler.class);

	public ClientHandler(Socket clientSocket, ServerRegistry socketServer) {
		super();
		this.clientSocket = clientSocket;
		this.socketServer = socketServer;

		try {
			socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
			socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			logger.error("Socket error ", e);
		}
	}

	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ClientHandler other) {
		return clientId.compareTo(other.getClientId());
	}

	public Integer getClientId() {
		return clientId;
	}

	public Socket getClientSocket() {
		return clientSocket;
	}

	public PipedOutputStream getEventStream() {
		return eventStream;
	}

	public Set<ClientHandler> getFollowers() {
		return followers;
	}

	private void handleEvents() {
		String event;
		try {
			while ((event = eventIn.readLine()) != null) {
				eventCounter++;
				socketOut.append(event);
			}
		} catch (IOException e) {
			logger.error("Error reading event", e);
		}
	}

	@Override
	public void run() {
		try {
			clientId = Integer.valueOf(socketIn.readLine());
			socketServer.registerClient(this);
			eventIn = new BufferedReader(new InputStreamReader(new PipedInputStream(eventStream)));
		} catch (IOException e) {
			logger.error("Error executing input", e);
		}
		handleEvents();
	}


	public void setEventStream(PipedOutputStream eventStream) {
		this.eventStream = eventStream;
	}

}
