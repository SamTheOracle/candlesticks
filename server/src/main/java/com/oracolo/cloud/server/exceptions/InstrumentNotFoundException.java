package com.oracolo.cloud.server.exceptions;

public class InstrumentNotFoundException extends RuntimeException{

	public InstrumentNotFoundException() {
		super("No instrument found for given isin");
	}
}
