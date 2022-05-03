package com.oracolo.cloud.server;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.oracolo.cloud.entities.sql.CandleStick;
import com.oracolo.cloud.entities.sql.Instrument;
import com.oracolo.cloud.events.CandlestickQuote;
import com.oracolo.cloud.streamhandler.QuotedInstrument;
import com.oracolo.cloud.streamhandler.StreamHandler;

import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class CandleStickService {

	public static final int MINUTE_IN_SECONDS = 60;
	public static final double DEFAULT_PRICE = 0.0;

	@Inject
	StreamHandler streamHandler;

	@ConfigProperty(name = "CANDLESTICK_TIME_WINDOW_SECONDS", defaultValue = "300")
	int candlestickTimeWindowSeconds;

	public List<CandleStick> getCandlesticksByIsin(String isin) {
		Instant currentMinute = currentMinuteInstant();
		Instant candlestickWindowAgo = Instant.from(currentMinute.minusSeconds(candlestickTimeWindowSeconds));
		return CandleStick.findByIsinAndRange(isin, currentMinute, candlestickWindowAgo);
	}

	@Scheduled(every = "5s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
	@Transactional
	public void grindData() {
		Instant closeTimestamp = currentMinuteInstant();
		Instant openTimestamp = closeTimestamp.minusSeconds(MINUTE_IN_SECONDS);
		List<QuotedInstrument> quotedInstruments = streamHandler.fetchStream();
		Map<String, Instrument> isinCache = new HashMap<>();
		for (QuotedInstrument quote : quotedInstruments) {
			Instrument instrument = isinCache.computeIfAbsent(quote.isin(), isin -> {
				Optional<Instrument> instrumentOptional = Instrument.findByIdOptional(isin);
				if (instrumentOptional.isEmpty()) {
					Instrument instr = new Instrument();
					instr.setIsin(quote.isin());
					instr.persist();
					return instr;
				}
				return instrumentOptional.get();
			});
			List<CandlestickQuote> quotesByIsin = quotedInstruments.stream().filter(q -> q.isin().equals(quote.isin())).flatMap(
					quotedInstrument -> quotedInstrument.quotes().stream()).collect(Collectors.toUnmodifiableList());
			handleCandlestick(instrument, openTimestamp, closeTimestamp, quotesByIsin);
		}

	}

	private void handleCandlestick(Instrument instrument, Instant openTimestamp, Instant closeTimestamp,
			List<CandlestickQuote> quotesByIsin) {
		Optional<CandleStick> candleStickOptional = CandleStick.findByOpenTimestamp(openTimestamp);
		CandleStick candleStick;
		if (candleStickOptional.isEmpty()) {
			candleStick = new CandleStick();
			candleStick.setInstrument(instrument);
			candleStick.persist();
		} else {
			candleStick = candleStickOptional.get();
		}
		double openPrice = quotesByIsin.stream().reduce((quote1, quote2) -> quote1.timestamp() < quote2.timestamp() ? quote1 : quote2).map(
				CandlestickQuote::price).orElse(DEFAULT_PRICE);
		double closePrice = quotesByIsin.stream().reduce((quote1, quote2) -> quote1.timestamp() > quote2.timestamp() ? quote1 : quote2).map(
				CandlestickQuote::price).orElse(DEFAULT_PRICE);
		double highPrice = quotesByIsin.stream().mapToDouble(CandlestickQuote::price).max().orElse(DEFAULT_PRICE);
		double lowPrice = quotesByIsin.stream().mapToDouble(CandlestickQuote::price).min().orElse(DEFAULT_PRICE);
		candleStick.setClosePrice(closePrice);
		candleStick.setOpenPrice(openPrice);
		candleStick.setHighPrice(highPrice);
		candleStick.setLowPrice(lowPrice);
		candleStick.setOpenTimestamp(openTimestamp);
		candleStick.setCloseTimestamp(closeTimestamp);
		CandleStick.updateFully(candleStick);
	}

	private static Instant currentMinuteInstant(){
		return  ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).withSecond(0).withNano(0).toInstant();
	}

}
