package com.oracolo.cloud.streamhandler.internal;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import com.oracolo.cloud.entities.Instrument;
import com.oracolo.cloud.entities.Quote;
import com.oracolo.cloud.events.CandleStickInstrument;
import com.oracolo.cloud.events.CandleStickQuote;
import com.oracolo.cloud.events.InstrumentEventType;
import com.oracolo.cloud.streamhandler.QuotedInstrument;
import com.oracolo.cloud.streamhandler.StreamHandler;

/**
 * An handler implementing {@link StreamHandler} in an async fashion
 */
@ApplicationScoped
class StreamHandlerImpl implements StreamHandler {
	@Override
	public void handleInstrumentEvent(CandleStickInstrument candlestickInstrument) {
		Instant timestamp = Instant.now();
		if (candlestickInstrument.type() == InstrumentEventType.DELETE) {
			Instrument.deleteByIsinTimestamp(candlestickInstrument.isin(), timestamp.toEpochMilli());
			return;
		}
		Instrument instrument = Instrument.from(candlestickInstrument);
		instrument.persist();
	}

	@Override
	public void handleQuoteEvent(CandleStickQuote quoteEvent) {
		Quote quote = Quote.from(quoteEvent);
		quote.persist();
	}

	@Override
	public List<QuotedInstrument> fetchStream(Instant from, Instant to) {
		List<Instrument> instruments = Instrument.findByRange(from.toEpochMilli(), to.toEpochMilli());
		List<Quote> quotes = Quote.findByIsinsInRange(
				instruments.stream().map(Instrument::getIsin).collect(Collectors.toUnmodifiableSet()),
				from.toEpochMilli(), to.toEpochMilli());
		return instruments.parallelStream().map(
				instrumentStream -> new InstrumentWithQuote(instrumentStream.getIsin(), instrumentStream.getDescription(),
						quotes.parallelStream().filter(quote -> quote.isin().equals(instrumentStream.getIsin())).collect(
								Collectors.toUnmodifiableList()))).collect(Collectors.toUnmodifiableList());
	}

}
