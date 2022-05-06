package com.oracolo.cloud.streamhandler;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Future;

import com.oracolo.cloud.events.CandlestickInstrument;
import com.oracolo.cloud.events.CandlestickQuote;

/**
 * Handles stream of data from provider.
 */
public interface StreamHandler {
	/**
	 * Generic, free for all method that handles a {@link CandlestickInstrument} coming from provider.
	 * @param instrumentEvent an instance of {@link CandlestickInstrument}
	 */
	void handleInstrumentEvent(CandlestickInstrument instrumentEvent);

	/**
	 * Generic, free for all method that handles a {@link CandlestickQuote} coming from provider
	 * @param quoteEvent an instance of {@link CandlestickQuote}
	 */
	void handleQuoteEvent(CandlestickQuote quoteEvent);

	/**
	 * It fetches all data in the given range
	 * @return an instance of {@link QuotedInstrument} in an immutable {@link List}. It could be empty or has instruments with no quote
	 */
	List<QuotedInstrument> fetchStream(Instant from, Instant to);
}
