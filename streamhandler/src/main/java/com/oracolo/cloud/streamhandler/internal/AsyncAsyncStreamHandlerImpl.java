package com.oracolo.cloud.streamhandler.internal;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.context.ManagedExecutor;

import com.oracolo.cloud.entities.InstrumentStream;
import com.oracolo.cloud.entities.QuoteStream;
import com.oracolo.cloud.events.CandlestickInstrument;
import com.oracolo.cloud.events.CandlestickQuote;
import com.oracolo.cloud.events.InstrumentEventType;
import com.oracolo.cloud.streamhandler.AsyncStreamHandler;
import com.oracolo.cloud.streamhandler.QuotedInstrument;

/**
 * An handler implementing {@link AsyncStreamHandler} in an async fashion
 */
@ApplicationScoped
class AsyncAsyncStreamHandlerImpl implements AsyncStreamHandler {
	/**
	 * 1 minute in milliseconds
	 */
	private static final long MINUTE_IN_MILLIS = 60 * 2 * 1000;

	@Inject
	ManagedExecutor executor;

	@Override
	public Future<Void> handleInstrumentEvent(CandlestickInstrument instrumentEvent) {
		return executor.runAsync(() -> handleInstrument(instrumentEvent));
	}

	@Override
	public Future<Void> handleQuoteEvent(CandlestickQuote quoteEvent) {
		return executor.runAsync(() -> handleQuote(quoteEvent));
	}

	@Override
	public List<QuotedInstrument> fetchStream() {
		Instant now = Instant.now();
		Instant oneMinuteAgo = now.minusMillis(MINUTE_IN_MILLIS);
		List<InstrumentStream> instrumentStreams = InstrumentStream.findInstrumentsByRange(oneMinuteAgo.toEpochMilli(), now.toEpochMilli());
		List<QuoteStream> quotes = QuoteStream.findQuotasByIsinInRange(
				instrumentStreams.stream().map(InstrumentStream::getIsin).collect(Collectors.toUnmodifiableSet()),
				oneMinuteAgo.toEpochMilli(), now.toEpochMilli());
		return instrumentStreams.stream().map(
				instrumentStream -> new InstrumentWithQuote(instrumentStream.getIsin(), instrumentStream.getDescription(), instrumentStream.getTimestamp(), quotes.stream().filter(quote -> quote.isin().equals(instrumentStream.getIsin())).collect(
								Collectors.toUnmodifiableList()))).collect(Collectors.toUnmodifiableList());
	}

	private static void handleInstrument(CandlestickInstrument candlestickInstrument) {
		Instant timestamp = Instant.now();
		if (candlestickInstrument.type() == InstrumentEventType.DELETE) {
			InstrumentStream.delete("isin=?1 and timestamp<=?2", candlestickInstrument.isin(), timestamp.toEpochMilli());
			QuoteStream.delete("isin=?1 and timestamp<=?2", candlestickInstrument.isin(), timestamp.toEpochMilli());
			return;
		}
		InstrumentStream instrumentStream = InstrumentStream.from(candlestickInstrument);
		instrumentStream.persist();
	}

	private static void handleQuote(CandlestickQuote candlestickQuote) {
		QuoteStream quote = QuoteStream.from(candlestickQuote);
		quote.persist();
	}
}
