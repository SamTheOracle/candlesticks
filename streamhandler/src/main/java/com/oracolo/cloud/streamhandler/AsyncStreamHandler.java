package com.oracolo.cloud.streamhandler;

import java.util.List;
import java.util.concurrent.Future;

import com.oracolo.cloud.events.CandlestickInstrument;
import com.oracolo.cloud.events.CandlestickQuote;

/**
 * Handles stream of data from provider.
 */
public interface AsyncStreamHandler {
	/**
	 * Generic, free for all method that handles a {@link CandlestickInstrument} coming from provider.
	 * @param instrumentEvent an instance of {@link CandlestickInstrument}
	 */
	Future<Void> handleInstrumentEvent(CandlestickInstrument instrumentEvent);

	/**
	 * Generic, free for all method that handles a {@link CandlestickQuote} coming from provider
	 * @param quoteEvent an instance of {@link CandlestickQuote}
	 */
	Future<Void> handleQuoteEvent(CandlestickQuote quoteEvent);

	/**
	 * It fetches all data in a minute time-window
	 * @return an instance of {@link QuotedInstrument} in an immutable {@link List}, potentially empty
	 */
	List<QuotedInstrument> fetchStream();
}
