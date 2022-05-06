package com.oracolo.cloud.streamhandler.internal;

import java.util.List;

import com.oracolo.cloud.events.CandleStickQuote;
import com.oracolo.cloud.events.InstrumentEventType;
import com.oracolo.cloud.streamhandler.QuotedInstrument;

class InstrumentWithQuote implements QuotedInstrument {
	private final String isin, description;
	private final List<CandleStickQuote> quoteData;

	InstrumentWithQuote(String isin, String description, List<CandleStickQuote> quotes) {
		this.isin = isin;
		this.description = description;
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
	public List<CandleStickQuote> quotes() {
		return quoteData;
	}

	@Override
	public String toString() {
		return "InstrumentWithQuote{" + "isin='" + isin + '\'' + ", description='" + description + '\'' + ", quoteData=" + quoteData + '}';
	}
}
