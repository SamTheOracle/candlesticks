package com.oracolo.cloud.server.exceptions.em;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.oracolo.cloud.server.dto.ErrorDto;
import com.oracolo.cloud.server.exceptions.ErrorCode;
import com.oracolo.cloud.server.exceptions.InstrumentNotFoundException;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class InstrumentNotFoundExceptionMapper implements ExceptionMapper<InstrumentNotFoundException> {
	@Override
	public Response toResponse(InstrumentNotFoundException exception) {
		return Response.status(Response.Status.NOT_FOUND).entity(new ErrorDto(ErrorCode.INSTRUMENT_NOT_FOUND.reason())).build();
	}
}
