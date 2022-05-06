package com.oracolo.cloud.server.exceptions;

public enum ErrorCode {

	INSTRUMENT_NOT_FOUND("missing_isin");

	private final String reason;

	ErrorCode(String reason) {
		this.reason = reason;
	}

	public String reason(){
		return reason;
	}
}
