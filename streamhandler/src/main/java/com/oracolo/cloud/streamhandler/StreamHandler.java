package com.oracolo.cloud.streamhandler;

import java.time.Instant;
import java.util.List;

import com.oracolo.cloud.events.CandleStickInstrument;
import com.oracolo.cloud.events.CandleStickQuote;

/**
 * Handles stream of data from provider.
 */
public interface StreamHandler {
	/**
	 * Generic, free for all method that handles a {@link CandleStickInstrument} coming from provider.
	 * @param instrumentEvent an instance of {@link CandleStickInstrument}
	 */
	void handleInstrumentEvent(CandleStickInstrument instrumentEvent);

	/**
	 * Generic, free for all method that handles a {@link CandleStickQuote} coming from provider
	 * @param quoteEvent an instance of {@link CandleStickQuote}
	 */
	void handleQuoteEvent(CandleStickQuote quoteEvent);

	/**
	 * It fetches all data in the given range
	 * @return an instance of {@link QuotedInstrument} in an immutable {@link List}. It could be empty or has instruments with no quote
	 */
	List<QuotedInstrument> fetchStream(Instant from, Instant to);
}
