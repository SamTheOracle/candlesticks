package com.oracolo.cloud.server.ws;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oracolo.cloud.events.CandlestickInstrument;
import com.oracolo.cloud.events.InstrumentEvent;
import com.oracolo.cloud.server.ws.decoders.InstrumentEventDecoder;
import com.oracolo.cloud.streamhandler.StreamHandler;

@ServerEndpoint(value = "/instruments", decoders = InstrumentEventDecoder.class)
public class InstrumentClient {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Inject
	StreamHandler streamHandler;

	@Inject
	ManagedExecutor managedExecutor;

	@Inject
	Event<CandlestickInstrument> candlestickInstrumentEvent;

	@OnOpen
	public void onOpen(Session session) {
		logger.info("Session {} open!", session.getId());
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		logger.info("Session {} closed because of {}", session.getId(), closeReason);
	}

	@OnError
	public void onError(Session session, Throwable t) {
		logger.info("Error for session {} because of {}", session.getId(), t.getMessage());
	}

	@OnMessage
	public void onMessage(InstrumentEvent instrumentEvent) {
		candlestickInstrumentEvent.fire(instrumentEvent);
		managedExecutor.submit(() -> streamHandler.handleInstrumentEvent(instrumentEvent));
	}
}
