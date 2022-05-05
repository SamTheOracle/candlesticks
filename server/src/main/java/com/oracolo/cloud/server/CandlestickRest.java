package com.oracolo.cloud.server;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.oracolo.cloud.server.dto.CandleStickDto;

@Path("/candlesticks")
public class CandlestickRest {

	@Inject
	CandleStickConverter candleStickConverter;

	@Inject
	CandleStickManager candleStickManager;

	@GET
	@Path("{isin}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<CandleStickDto> getCandlesticks(@PathParam("isin") String isin){
		return candleStickConverter.from(candleStickManager.getCandlesticksByIsin(isin));
	}
}
