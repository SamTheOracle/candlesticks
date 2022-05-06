package com.oracolo.cloud.server.dto;

import java.time.Instant;

import lombok.Builder;

@Builder
public class CandleStickDto implements Comparable<CandleStickDto> {
	public String openTimestamp, closeTimestamp;
	public double openPrice, closePrice, lowPrice, highPrice;

	@Override
	public int compareTo(CandleStickDto o) {
		return openTimestamp.compareTo(o.openTimestamp);
	}
}
