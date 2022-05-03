package com.oracolo.cloud.server;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.oracolo.cloud.entities.QuoteStream;
import com.oracolo.cloud.entities.sql.CandleStick;
import com.oracolo.cloud.entities.sql.Instrument;
import com.oracolo.cloud.server.dao.CandlestickDao;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class CandleStickService {

	public static final int MINUTE_IN_SECONDS = 60;
	public static final double DEFAULT_PRICE = 0.0;

	@Inject
	CandlestickDao candlestickDao;

	@ConfigProperty(name = "CANDLESTICK_TIME_WINDOW_SECONDS", defaultValue = "300")
	int candlestickTimeWindowSeconds;

	public List<CandleStick> getCandlesticksByIsin(String isin) {
		Instant currentMinute = Instant.from(LocalDateTime.now().withNano(0));
		Instant candlestickWindowAgo = Instant.from(currentMinute.minusSeconds(candlestickTimeWindowSeconds));
		return CandleStick.findByIsinAndRange(isin, currentMinute, candlestickWindowAgo);
	}

	@Scheduled(every = "5s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
	private void grindData() {
		LocalDateTime now = LocalDateTime.now();
		Instant currentMinuteAsTimestamp = Instant.from(now.withNano(0));
		Instant minuteTimestamp = currentMinuteAsTimestamp.minusSeconds(MINUTE_IN_SECONDS);
		List<QuoteStream> quoteStreamInLastMinute = QuoteStream.findQuotesInRange(minuteTimestamp.toEpochMilli(),
				currentMinuteAsTimestamp.toEpochMilli());
		Map<String, Instrument> isinCache = new HashMap<>();
		for (QuoteStream quote : quoteStreamInLastMinute) {
			Instrument instrument = isinCache.computeIfAbsent(quote.isin(),isin->{
				Optional<Instrument> instrumentOptional = Instrument.findByIdOptional(isin);
				if(instrumentOptional.isEmpty()){
					Instrument instr = new Instrument();
					instr.setIsin(quote.isin());
					instr.persist();
					return instr;
				}
				return instrumentOptional.get();
			});
			Optional<CandleStick> candleStickOptional = candlestickDao.getCandlestickForOpenTimestamp(minuteTimestamp);
			CandleStick candleStick;
			if (candleStickOptional.isEmpty()) {
				candleStick = new CandleStick();
				candleStick.setInstrument(instrument);
				candleStick.persist();
			} else {
				candleStick = candleStickOptional.get();
			}
			List<QuoteStream> quotesByIsin = quoteStreamInLastMinute.stream().filter(q -> q.isin().equals(quote.isin())).collect(
					Collectors.toUnmodifiableList());
			double openPrice = quotesByIsin.stream().reduce(
					(quote1, quote2) -> quote1.getTimestamp() < quote2.getTimestamp() ? quote1 : quote2).map(QuoteStream::getPrice).orElse(
					DEFAULT_PRICE);
			double closePrice = quotesByIsin.stream().reduce(
					(quote1, quote2) -> quote1.getTimestamp() > quote2.getTimestamp() ? quote1 : quote2).map(QuoteStream::getPrice).orElse(
					DEFAULT_PRICE);
			double highPrice = quotesByIsin.stream().mapToDouble(QuoteStream::getPrice).max().orElse(DEFAULT_PRICE);
			double lowPrice = quotesByIsin.stream().mapToDouble(QuoteStream::getPrice).max().orElse(DEFAULT_PRICE);
			candleStick.setClosePrice(closePrice);
			candleStick.setOpenPrice(openPrice);
			candleStick.setHighPrice(highPrice);
			candleStick.setLowPrice(lowPrice);
			candleStick.setOpenTimestamp(minuteTimestamp);
			candleStick.setCloseTimestamp(currentMinuteAsTimestamp);
			CandleStick.updateFully(candleStick);
		}

	}

}
