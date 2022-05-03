package com.oracolo.cloud.entities.sql;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@QuarkusTest
@TestMethodOrder(MethodOrderer.Random.class)
class PersistenceCandleStickTest {

    @Test
    @DisplayName("Should full update")
    @TestTransaction
    void updateFully() {
        String isin = UUID.randomUUID().toString();
        Instant close = currentMinuteTimestamp();
        Instant open = close.minusSeconds(60);
        Instrument instrument = Instrument.builder().isin(isin).build();
        instrument.persist();
        Random random = new Random();
        double closePrice = random.nextDouble();
        CandleStick candleStick = CandleStick.builder()
                .instrument(instrument)
                .openTimestamp(open)
                .closeTimestamp(close)
                .closePrice(closePrice)
                .build();
        testCreate(candleStick);
        candleStick.setClosePrice(random.nextDouble());
        CandleStick.updateFully(candleStick);
        Assertions.assertNotEquals(closePrice,candleStick.getClosePrice());

    }

    @Test
    @DisplayName("Should find candlesticks in given range")
    @TestTransaction
    void findByIsinAndRange() {
        String isin = UUID.randomUUID().toString();
        Instant close = currentMinuteTimestamp();
        Instant open = close.minusSeconds(60);
        Instrument instrument = Instrument.builder().isin(isin).build();
        instrument.persist();
        CandleStick candleStick1 = CandleStick.builder()
                .instrument(instrument)
                .openTimestamp(open)
                .closeTimestamp(close)
                .build();
        CandleStick candleStick2 = CandleStick.builder()
                .instrument(instrument)
                .openTimestamp(open)
                .closeTimestamp(close)
                .build();
        testCreate(candleStick1);
        testCreate(candleStick2);
        List<CandleStick> candleSticksInRange = CandleStick.findByIsinAndRange(isin,open,close);
        Assertions.assertFalse(candleSticksInRange.isEmpty());
        Assertions.assertTrue(candleSticksInRange.stream().anyMatch(candleStick -> candleStick.equals(candleStick1) || candleStick.equals(candleStick2)));
    }

    @Test
    @DisplayName("Should find candlesticks by open timestamp")
    @TestTransaction
    void findByOpenTimestamp() {
        Instant close = currentMinuteTimestamp();
        Instant open = close.minusSeconds(60);
        CandleStick candleStick = CandleStick.builder().openTimestamp(open).closeTimestamp(close).build();
        candleStick.persist();
        Optional<CandleStick> candleStickOptional = CandleStick.findByOpenTimestamp(open);
        Assertions.assertTrue(candleStickOptional.isPresent());
        CandleStick persistedCandlestick = candleStickOptional.get();
        assertCandlestick(candleStick, persistedCandlestick);

    }

    @Test
    @DisplayName("Should persist a candlestick")
    @TestTransaction
    void shouldPersistCandlestick() {
        Instant currentMinuteTimestamp = currentMinuteTimestamp();
        Instant minusMinute = currentMinuteTimestamp.minusSeconds(60);
        double openPrice = 123.21;
        double closePrice = 11;
        double lowPrice = 7;
        double highPrice = 187.5;
        CandleStick candleStick = CandleStick.builder().openPrice(openPrice)
                .closePrice(closePrice)
                .lowPrice(lowPrice)
                .highPrice(highPrice)
                .openTimestamp(minusMinute)
                .closeTimestamp(currentMinuteTimestamp)
                .build();
        testCreate(candleStick);

    }

    private static CandleStick testCreate(CandleStick candleStick) {
        candleStick.persist();
        Optional<CandleStick> candleStickOptional = CandleStick.findByIdOptional(candleStick.id);
        Assertions.assertTrue(candleStickOptional.isPresent());
        CandleStick persistedCandlestick = candleStickOptional.get();
        Assertions.assertNotNull(persistedCandlestick);
        assertCandlestick(candleStick, persistedCandlestick);
        return persistedCandlestick;
    }

    private static void assertCandlestick(CandleStick first, CandleStick second) {
        Assertions.assertEquals(first.id, second.id);
        Assertions.assertEquals(first.getOpenPrice(), second.getOpenPrice());
        Assertions.assertEquals(first.getClosePrice(), second.getClosePrice());
        Assertions.assertEquals(first.getHighPrice(), second.getHighPrice());
        Assertions.assertEquals(first.getLowPrice(), second.getLowPrice());
        Assertions.assertEquals(first.getOpenTimestamp(), second.getOpenTimestamp());
        Assertions.assertEquals(first.getCloseTimestamp(), second.getCloseTimestamp());
    }

    private static Instant currentMinuteTimestamp() {
        ZonedDateTime instant = ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        return Assertions.assertDoesNotThrow(() -> Instant.from(instant.withSecond(0).withNano(0)));
    }
}