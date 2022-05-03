package com.oracolo.cloud.events;

import java.time.Instant;

public class Quote implements CandlestickQuote {
	public String type;
	public QuoteData data;

	@Override
	public String toString() {
		return "QuoteEvent{" + "type='" + type + '\'' + ", data=" + data + '}';
	}

	@Override
	public String isin() {
		return data.isin;
	}

	@Override
	public double price() {
		return data.price;
	}

	@Override
	public long timestamp() {
		return Instant.now().toEpochMilli();
	}
}
