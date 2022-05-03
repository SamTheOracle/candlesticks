package com.oracolo.cloud.streamhandler.internal;

import java.util.List;

import com.oracolo.cloud.events.CandlestickQuote;
import com.oracolo.cloud.events.InstrumentEventType;
import com.oracolo.cloud.streamhandler.QuotedInstrument;

class InstrumentWithQuote implements QuotedInstrument {
	private final String isin, description;
	private final long timestamp;
	private final List<CandlestickQuote> quoteData;

	InstrumentWithQuote(String isin, String description, long timestamp, List<CandlestickQuote> quotes) {
		this.isin = isin;
		this.description = description;
		this.timestamp = timestamp;
		this.quoteData = quotes;
	}

	@Override
	public String isin() {
		return isin;
	}

	@Override
	public InstrumentEventType type() {
		return InstrumentEventType.ADD;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public long timestamp() {
		return timestamp;
	}

	@Override
	public List<CandlestickQuote> quotes() {
		return quoteData;
	}

	@Override
	public String toString() {
		return "InstrumentWithQuote{" + "isin='" + isin + '\'' + ", description='" + description + '\'' + ", quoteData=" + quoteData + '}';
	}
}
