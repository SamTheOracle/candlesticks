package com.oracolo.cloud.server.ws;

import java.io.IOException;
import java.net.URI;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oracolo.cloud.events.CandlestickInstrument;
import com.oracolo.cloud.events.InstrumentEvent;
import com.oracolo.cloud.events.QuoteEvent;
import com.oracolo.cloud.streamhandler.StreamHandler;

import io.quarkus.arc.profile.UnlessBuildProfile;
import io.quarkus.runtime.Startup;
import io.vertx.core.json.Json;

@ClientEndpoint
@Startup
@UnlessBuildProfile(value = "test")
public class QuoteClient {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Inject
	StreamHandler streamHandler;

	@Inject
	ManagedExecutor managedExecutor;

	@Inject
	Event<CloseReason> closeConnectionEvent;

	@ConfigProperty(name = "WEBSOCKET_QUOTE_URI", defaultValue = "ws://localhost:8032/quotes")
	String connection;

	@ConfigProperty(name = "MAX_CONNECTION_RETRY", defaultValue = "10")
	int maxConnectionRetry;

	@ConfigProperty(name = "CONNECTION_RETRY_INTERVAL_MILLI", defaultValue = "10")
	int retryInterval;

	@PostConstruct
	public void start() throws DeploymentException, InterruptedException {
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		URI uri = URI.create(connection);
		Session session = null;
		int retryCount = 0;
		while (session == null && (retryInterval <= maxConnectionRetry)) {
			try {
				session = container.connectToServer(this, uri);
			} catch (IOException e) {
				logger.error("Error opening websocket client to {}", uri, e);
				Thread.sleep(retryInterval);
			}
		}
		if(retryCount > maxConnectionRetry){
			logger.error("Error. Could not connect to server {}", uri);
		}
	}

	@OnOpen
	public void onOpen(Session session) {
		logger.info("Session {} open!", session.getId());
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		logger.info("Session {} closed because of {}", session.getId(), closeReason);
		closeConnectionEvent.fire(closeReason);
	}

	@OnError
	public void onError(Session session, Throwable t) {
		logger.debug("Error for session {} because of {}", session.getId(), t.getMessage());
	}

	@OnMessage
	public void onMessage(String message) {
		logger.debug("Received {}", message);
		managedExecutor.submit(() -> {
			QuoteEvent quoteEvent = Json.decodeValue(message, QuoteEvent.class);
			streamHandler.handleQuoteEvent(quoteEvent);
		});
	}
}
