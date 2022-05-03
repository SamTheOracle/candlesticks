package com.oracolo.cloud.server;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.oracolo.cloud.streamhandler.StreamHandler;

@Path("/candlesticks")
public class CandlestickRest {


	@GET
	@Path("{isin}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCandlesticks(){
		return "hello";
	}
}
