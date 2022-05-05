package com.oracolo.cloud.server;

import static com.oracolo.cloud.entities.CurrentTimeStampUtils.currentMinuteTimestamp;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.oracolo.cloud.entities.sql.CandleStick;
import com.oracolo.cloud.entities.sql.Instrument;
import com.oracolo.cloud.events.CandlestickQuote;
import com.oracolo.cloud.streamhandler.QuotedInstrument;
import com.oracolo.cloud.streamhandler.StreamHandler;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class CandleStickManager {

	public static final int MINUTE_IN_SECONDS = 60;
	public static final double DEFAULT_PRICE = 0.0;

	@Inject
	StreamHandler streamHandler;

	@ConfigProperty(name = "CANDLESTICK_TIME_WINDOW_SECONDS", defaultValue = "300")
	int candlestickTimeWindowSeconds;

	public List<CandleStick> getCandlesticksByIsin(String isin) {
		Instant currentMinute = currentMinuteTimestamp();
		Instant candlestickWindowAgo = Instant.from(currentMinute.minusSeconds(candlestickTimeWindowSeconds));
		List<CandleStick> candleSticks = CandleStick.findByIsinAndRange(isin, candlestickWindowAgo, currentMinute);
		int timeWindowMinute = candlestickTimeWindowSeconds/60;
		if(candleSticks.size() < timeWindowMinute){
			candlestickWindowAgo = candlestickWindowAgo.minusSeconds(candlestickTimeWindowSeconds);
			candleSticks = CandleStick.findByIsinAndRange(isin, candlestickWindowAgo, currentMinute);
		}
		return candleSticks;
	}

	@Scheduled(every = "{candlestick.grind.period}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
	public void grindData() {
		List<QuotedInstrument> quotedInstruments = streamHandler.fetchStream();
		Instant closeTimestamp = currentMinuteTimestamp();
		Instant openTimestamp = closeTimestamp.minusSeconds(MINUTE_IN_SECONDS);
		Map<String, Instrument> isinCache = new HashMap<>();
		QuarkusTransaction.run(() -> quotedInstruments.forEach(quote -> handleQuote(quote, openTimestamp, closeTimestamp, isinCache)));
	}

	private void handleQuote(QuotedInstrument quote, Instant openTimestamp, Instant closeTimestamp, Map<String, Instrument> isinCache) {
		Instrument instrument = isinCache.computeIfAbsent(quote.isin(), isin -> {
			Optional<Instrument> instrumentOptional = Instrument.findByIdOptional(isin);
			if (instrumentOptional.isEmpty()) {
				Instrument instr = new Instrument();
				instr.setIsin(quote.isin());
				instr.setTimestamp(Instant.now());
				instr.setDescription(quote.description());
				instr.persist();
				return instr;
			}
			return instrumentOptional.get();
		});
		List<CandlestickQuote> quotesByIsin = quote.quotes();
		handleCandlestick(instrument, openTimestamp, closeTimestamp, quotesByIsin);
	}

	private void handleCandlestick(Instrument instrument, Instant openTimestamp, Instant closeTimestamp,
			List<CandlestickQuote> quotesByIsin) {
		Optional<CandleStick> candleStickOptional = CandleStick.findByOpenTimestamp(openTimestamp, instrument);
		CandleStick candleStick;
		if (candleStickOptional.isEmpty()) {
			candleStick = CandleStick.builder().instrument(instrument).closeTimestamp(closeTimestamp)
					.openTimestamp(openTimestamp)
					.closePrice(0.0)
					.highPrice(0.0)
					.lowPrice(0.0)
					.openPrice(0.0)
					.build();
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

}
