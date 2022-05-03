package com.oracolo.cloud.server.dto;

import com.oracolo.cloud.server.exceptions.ErrorCode;

public class ErrorDto {

	public String message;

	public ErrorCode code;

	public ErrorDto(ErrorCode code, String message) {
		this.message = message;
		this.code = code;
	}

	@Override
	public String toString() {
		return "ErrorDto{" + "message='" + message + '\'' + ", code=" + code + '}';
	}
}
