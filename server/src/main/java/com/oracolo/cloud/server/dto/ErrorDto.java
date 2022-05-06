package com.oracolo.cloud.server.dto;

import com.oracolo.cloud.server.exceptions.ErrorCode;

public class ErrorDto {


	public String reason;

	public ErrorDto(String reason) {
		this.reason = reason;
	}

	@Override
	public String toString() {
		return "ErrorDto{" +
				"reason=" + reason +
				'}';
	}
}
