package com.oracolo.cloud.server;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.oracolo.cloud.streamhandler.AsyncStreamHandler;

@Path("/hello")
public class CandlestickRest {

	@Inject
	AsyncStreamHandler asyncStreamHandler;

	@GET
	public String hello(){
		asyncStreamHandler.handleQuoteEvent(null);
		return "hello";

	}
}
