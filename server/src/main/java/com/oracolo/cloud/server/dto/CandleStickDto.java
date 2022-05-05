package com.oracolo.cloud.server.dto;

import java.time.Instant;

import lombok.Builder;

@Builder
public class CandleStickDto {
	public String openTimestamp, closeTimestamp;
	public double openPrice, closePrice, lowPrice, highPrice;

}
