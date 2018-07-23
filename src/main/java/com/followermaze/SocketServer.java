package com.followermaze;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;

import com.followermaze.event.Event;
import com.followermaze.handler.EventHandler;
import com.followermaze.handler.ClientHandler;
import com.followermaze.utils.PropertyUtils;
import org.apache.log4j.Logger;


public class SocketServer implements ServerRegistry {
	static Logger logger = Logger.getLogger(SocketServer.class);

	static final String eventListenerPortDefault = "9090";
	static final String clientListenerPortPropertyName = "clientListenerPort";
	static final String eventListenerPortPropertyName = "eventListenerPort";
	static final String clientListenerPortDefault = "9099";

	private int clientListenerPort;
	private int eventListenerPort;
	private Map<Integer, ClientHandler> clientConnections = new HashMap<Integer, ClientHandler>();
	private long eventQueueCounter = 1;
	private PriorityQueue<Event> eventPriorityQueue = new PriorityQueue<>();

	public Map<Integer, ClientHandler> getClientConnections() {
		return clientConnections;
	}

	public void setClientConnections(
			Map<Integer, ClientHandler> clientConnections) {
		this.clientConnections = clientConnections;
	}

	EventHandler eventHandler;

	public SocketServer() {
		Properties prop = PropertyUtils.getProperties();

		clientListenerPort = Integer.parseInt(prop.getProperty(clientListenerPortPropertyName,
				clientListenerPortDefault));
		eventListenerPort = Integer.parseInt(prop.getProperty(eventListenerPortPropertyName,
				eventListenerPortDefault));
	}

	/**
	 * Open event handler socket
	 */
	void startEventConnection() {
		EventHandler eventHandler = new EventHandler(this);
		logger.debug("opening port " + eventListenerPort);
		eventHandler.openEventSocket(eventListenerPort);
		eventHandler.start();
	}

	/**
	 * Open a server socket waiting for the clients to register.
	 *
	 * @return
	 * @throws IOException
	 */
	ServerSocket listenForConnectingClients() throws IOException {
		logger.info("listening on port " + clientListenerPort);
		ServerSocket clientListeningSocket = new ServerSocket(
				clientListenerPort);
		try {
			while (!clientListeningSocket.isClosed()) {
				Socket clientSocket = clientListeningSocket.accept();
				logger.info("connected to user Client on remote port "
						+ clientSocket.getPort());
				ClientHandler clientHandler = new ClientHandler(clientSocket, this);
				clientHandler.start();
			}
		} catch (Exception e) {
			logger.error("SS1 Exception when opening socket " + e.toString(), e);
		} finally {
			clientListeningSocket.close();
		}
		return null;
	}

	public void registerClient(ClientHandler clientHandler) {
		logger.info("trying to register client " + clientHandler.getId());
		synchronized (clientConnections) {
			PipedOutputStream eventStream = new PipedOutputStream();
			clientHandler.setEventStream(eventStream);
			clientConnections.put(clientHandler.getClientId(), clientHandler);
			logger.info("Socket server registered client " + clientHandler.getId());
		}
	}

	public void registerEventSource(EventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}


	public void handleEvent(String eventString, int eventCounter)
			throws IOException {
		Event event = new Event(eventString);
		logger.debug("eventNo " + eventCounter + " payload " + eventString.toString());
		ClientHandler fromClientHandler = clientConnections.get(event.getFromUserId());
		ClientHandler toClientHandler = clientConnections.get(event.getToUserId());

		eventPriorityQueue.add(event);
		Event queueHead = eventPriorityQueue.peek();

		while (queueHead != null
				&& queueHead.getSequenceNo() == eventQueueCounter) {
			switch (queueHead.getType()) {
				case BROADCAST:
					for (ClientHandler ucHandler : clientConnections.values()) {
						ucHandler.getEventStream().write(eventString.getBytes());
						ucHandler.getEventStream().flush();
					}
					break;
				case FOLLOW:
					if (fromClientHandler != null && toClientHandler != null)
						fromClientHandler.getFollowers().add(toClientHandler);
					// fall through
				case PRIVATE_MSG:
					if (toClientHandler != null) {
						toClientHandler.getEventStream().write(eventString.getBytes());
						toClientHandler.getEventStream().flush();
					} else {
						logger.error("missing client Id for message " + eventString);
						return;
					}
					break;
				case UN_FOLLOW:
					if (fromClientHandler != null && toClientHandler != null) {
						fromClientHandler.getFollowers().remove(toClientHandler);
					} else {
						logger.error(" Client Id not found for message "
								+ eventString);
						return;
					}
					break;
				case STATUS_UPDATE:
					if (fromClientHandler != null) {
						for (ClientHandler uct : fromClientHandler.getFollowers()) {
							uct.getEventStream().write(eventString.getBytes());
							uct.getEventStream().flush();
						}
					}
					break;
				default:
					logger.error("Unknown event type " + eventString);
			}
			// remove the head from the queue and peek at next one
			eventPriorityQueue.remove();
			queueHead = eventPriorityQueue.peek();
			eventQueueCounter++; // event processed, deal with next one
		}
		return; // wait for next event to arrive
	}


}
