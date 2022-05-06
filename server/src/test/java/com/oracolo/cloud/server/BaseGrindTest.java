package com.oracolo.cloud.server;

import com.oracolo.cloud.entities.CandleStick;
import com.oracolo.cloud.entities.Instrument;
import com.oracolo.cloud.entities.Quote;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@QuarkusTest
public class BaseGrindTest {

    protected static final Random RANDOM = new Random();
    protected static final int MAX_QUOTES = RANDOM.nextInt(150);

    protected static final int MAX_NUMBER_INSTRUMENT = 8;

    protected static final List<String> ISINS = IntStream.range(0, MAX_NUMBER_INSTRUMENT).mapToObj(
            value -> UUID.randomUUID().toString()).collect(Collectors.toUnmodifiableList());

    @ConfigProperty(name = "candlestick.grind.time-window-seconds")
    int grindTimeWindow;

    @AfterEach
    public void cleanUp() {
        Quote.deleteAll();
        Instrument.deleteAll();
        CandleStick.deleteAll();
        Assertions.assertEquals(0, Quote.count());
        Assertions.assertEquals(0, Instrument.count());
        Assertions.assertEquals(0, CandleStick.count());
    }

    protected  void addStreamData(Instant instant, Predicate<Long> skipMilli) {
        AtomicLong currentMinuteMilli = new AtomicLong(instant.toEpochMilli());
        ISINS.forEach(isin -> {
            Instrument instrument = Instrument.builder().isin(isin).timestamp(currentMinuteMilli.get())
                    .description(UUID.randomUUID().toString())
                    .build();
            instrument.persist();
        });
        List<Instrument> instruments = Instrument.listAll();
        Assertions.assertEquals(MAX_NUMBER_INSTRUMENT, instruments.size());
        int grindTimeWindowMinutes = grindTimeWindow / 60;
        IntStream.range(0, grindTimeWindowMinutes).forEach(minute -> {
            long current = currentMinuteMilli.get();
            boolean shouldSkip = skipMilli.test(current);
            if (!shouldSkip) {
                IntStream.range(0, MAX_QUOTES).parallel().forEach(value -> instruments.forEach(instrument -> {
                    String isin = instrument.getIsin();
                    double price = RANDOM.nextDouble() * 1000;
                    long oneMinuteAgoBound = current - 60 * 1000;
                    long timestamp = oneMinuteAgoBound + (long) (Math.random() * (current - oneMinuteAgoBound));
                    Quote quote = Quote.builder().isin(isin).price(price).timestamp(timestamp).build();
                    quote.persist();
                }));
                currentMinuteMilli.addAndGet(-60 * 1000);
            }

        });
        Assertions.assertEquals((long) MAX_QUOTES * grindTimeWindowMinutes * MAX_NUMBER_INSTRUMENT, Quote.count());
    }

    protected void addStreamData(Instant instant) {
        addStreamData(instant, milli -> false);

    }
}
