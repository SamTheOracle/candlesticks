package com.oracolo.cloud.server;

import com.oracolo.cloud.entities.CandleStick;
import com.oracolo.cloud.entities.Instrument;
import com.oracolo.cloud.events.CandleStickQuote;
import com.oracolo.cloud.server.exceptions.InstrumentNotFoundException;
import com.oracolo.cloud.streamhandler.QuotedInstrument;
import com.oracolo.cloud.streamhandler.StreamHandler;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.oracolo.cloud.entities.CurrentTimeStampUtils.currentMinuteTimestamp;
import static com.oracolo.cloud.entities.CurrentTimeStampUtils.instantRoundedAtMinute;

@ApplicationScoped
public class CandleStickManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    public static final int MINUTE_IN_SECONDS = 60;
    public static final double DEFAULT_PRICE = 0.0;

    @Inject
    StreamHandler streamHandler;

    @ConfigProperty(name = "candlestick.time-window-seconds", defaultValue = "1800")
    int candlestickTimeWindowSeconds;

    @ConfigProperty(name = "candlestick.grind.time-window-seconds", defaultValue = "300")
    int grindTimeWindow;

    public List<CandleStick> getCandlesticksByIsin(String isin) {
        Optional<Instrument> instrumentOptional = Instrument.findByIsin(isin);
        if (instrumentOptional.isEmpty()) {
            throw new InstrumentNotFoundException();
        }
        Instant currentMinute = currentMinuteTimestamp();
        Instant candlestickWindowAgo = Instant.from(currentMinute.minusSeconds(candlestickTimeWindowSeconds));
        List<CandleStick> candleSticks = CandleStick.findByIsinAndRange(isin, candlestickWindowAgo, currentMinute);
        int timeWindowMinute = candlestickTimeWindowSeconds / 60;
        if (candleSticks.size() < timeWindowMinute) {
            List<Instant> instants = generateTimeWindow(candlestickTimeWindowSeconds);
            List<Instant> missingInstants = instants.stream()
                    .filter(instant -> candleSticks.stream().noneMatch(candleStick -> candleStick.getOpenTimestamp() == instant.toEpochMilli()))
                    .map(instant -> instant.minusSeconds(candlestickTimeWindowSeconds))
                    .collect(Collectors.toUnmodifiableList());
            List<CandleStick> missingCandlesticksFromPreviousCandle = missingInstants.stream()
                    .map(instant -> CandleStick.findByOpenTimestampAndIsin(instant, isin)
                            .orElse(CandleStick.empty(isin, instant.plusSeconds(candlestickTimeWindowSeconds), instant.plusSeconds(candlestickTimeWindowSeconds).plusSeconds(MINUTE_IN_SECONDS))))
                    .collect(Collectors.toUnmodifiableList());
            candleSticks.addAll(missingCandlesticksFromPreviousCandle);
        }
        return candleSticks;
    }

    @Scheduled(every = "{candlestick.grind.period}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void grindData() {
        Instant closeTimestamp = currentMinuteTimestamp();
        Instant openTimestamp = closeTimestamp.minusSeconds(grindTimeWindow);
        logger.info("Fetching data for minute: open -> {}, close -> {}", openTimestamp, closeTimestamp);
        List<QuotedInstrument> quotedInstruments = streamHandler.fetchStream(openTimestamp, closeTimestamp);
        quotedInstruments.forEach(this::doGrind);
    }


    private void doGrind(QuotedInstrument quotedInstrument) {
        String isin = quotedInstrument.isin();
        List<CandleStickQuote> quotes = quotedInstrument.quotes();
        Map<Instant, List<CandleStickQuote>> quotesByIsinPerMinute = quotes.stream().collect(Collectors.groupingBy(candlestickQuote ->
                instantRoundedAtMinute(Instant.ofEpochMilli(candlestickQuote.timestamp()))));
        for (Map.Entry<Instant, List<CandleStickQuote>> entry : quotesByIsinPerMinute.entrySet()) {
            Instant open = entry.getKey();
            Instant close = open.plusSeconds(MINUTE_IN_SECONDS);
            Optional<CandleStick> candleStickOptional = CandleStick.findByOpenTimestampAndIsin(open, isin);
            CandleStick candleStick;
            if(candleStickOptional.isEmpty()){
                candleStick = CandleStick.empty(isin,open,close);
                candleStick.persist();
            }else{
                candleStick = candleStickOptional.get();
            }
            List<CandleStickQuote> quotesByIsin = entry.getValue();
            double openPrice = quotesByIsin.stream()
                    .reduce((quote1, quote2) -> quote1.timestamp() < quote2.timestamp() ? quote1 : quote2)
                    .map(CandleStickQuote::price).orElse(DEFAULT_PRICE);
            double closePrice = quotesByIsin.stream()
                    .reduce((quote1, quote2) -> quote1.timestamp() > quote2.timestamp() ? quote1 : quote2)
                    .map(CandleStickQuote::price).orElse(DEFAULT_PRICE);
            double highPrice = quotesByIsin.stream()
                    .mapToDouble(CandleStickQuote::price).max().orElse(DEFAULT_PRICE);
            double lowPrice = quotesByIsin.stream()
                    .mapToDouble(CandleStickQuote::price).min().orElse(DEFAULT_PRICE);
           candleStick.setHighPrice(highPrice);
           candleStick.setLowPrice(lowPrice);
           candleStick.setOpenPrice(openPrice);
           candleStick.setClosePrice(closePrice);
           candleStick.update();
        }

    }

    private static List<Instant> generateTimeWindow(int timeWindowInMinutes) {
        Instant current = currentMinuteTimestamp();
        Instant windowAgo = current.minusSeconds(timeWindowInMinutes);
        List<Instant> instants = new ArrayList<>();
        for (int step = 0; step < timeWindowInMinutes / 60; step++) {
            instants.add(windowAgo.plusSeconds((long) MINUTE_IN_SECONDS * step));
        }
        return instants;
    }

}
