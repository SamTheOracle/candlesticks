package com.oracolo.cloud.server.ws.decoders;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracolo.cloud.events.InstrumentEvent;

import io.vertx.core.json.Json;

public class InstrumentEventDecoder implements Decoder.Text<InstrumentEvent> {

	@Override
	public void init(EndpointConfig config) {

	}

	@Override
	public void destroy() {

	}

	@Override
	public InstrumentEvent decode(String s) throws DecodeException {
		return Json.decodeValue(s, InstrumentEvent.class);
	}

	@Override
	public boolean willDecode(String s) {
		return true;
	}
}
