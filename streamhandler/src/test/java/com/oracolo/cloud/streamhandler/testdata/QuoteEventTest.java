package com.oracolo.cloud.streamhandler.testdata;

import com.oracolo.cloud.events.CandlestickQuote;

public class QuoteEventTest implements CandlestickQuote {
	private final String isin;
	private final double price;
	private final long timestamp;

	public QuoteEventTest(String isin, double price, long timestamp) {
		this.isin = isin;
		this.price = price;
		this.timestamp=timestamp;
	}

	@Override
	public String isin() {
		return isin;
	}

	@Override
	public double price() {
		return price;
	}

	@Override
	public long timestamp() {
		return timestamp;
	}
}
