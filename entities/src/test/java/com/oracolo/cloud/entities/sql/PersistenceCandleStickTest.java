package com.oracolo.cloud.entities.sql;

import com.oracolo.cloud.entities.CandleStick;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static com.oracolo.cloud.entities.CurrentTimeStampUtils.currentMinuteTimestamp;

@QuarkusTest
@TestMethodOrder(MethodOrderer.Random.class)
class PersistenceCandleStickTest {

    private static final Random RANDOM = new Random();


    @Test
    @DisplayName("Should delete all candlestick with close timestamp less than given")
    public void shouldDeleteForGivenIsinClose() {
        Instant close = currentMinuteTimestamp();
        Instant open = close.minusSeconds(60);
        CandleStick candleStick1 = CandleStick.builder()
                .isin(UUID.randomUUID().toString())
                .openTimestamp(open.toEpochMilli())
                .closeTimestamp(close.toEpochMilli())
                .build();
        CandleStick candleStick2 = CandleStick.builder()
                .isin(UUID.randomUUID().toString())
                .openTimestamp(open.toEpochMilli())
                .closeTimestamp(close.toEpochMilli())
                .build();
        CandleStick candleStick3 = CandleStick.builder()
                .isin(UUID.randomUUID().toString())
                .openTimestamp(open.toEpochMilli())
                .closeTimestamp(close.toEpochMilli())
                .build();
        testCreate(candleStick1);
        testCreate(candleStick2);
        testCreate(candleStick3);
        CandleStick.deleteByIsinClose(candleStick1.getIsin(), close.toEpochMilli());
        List<CandleStick> candleSticks = CandleStick.listAll();
        Assertions.assertEquals(2, candleSticks.size());
        Assertions.assertTrue(candleSticks.stream().noneMatch(candleStick -> candleStick.getIsin().equals(candleStick1.getIsin())));
    }


    @Test
    @DisplayName("Should full update")
    void updateFully() {
        String isin = UUID.randomUUID().toString();
        Instant close = currentMinuteTimestamp();
        Instant open = close.minusSeconds(60);
        Random random = new Random();
        double closePrice = random.nextDouble();
        CandleStick candleStick = CandleStick.builder()
                .isin(isin)
                .openTimestamp(open.toEpochMilli())
                .closeTimestamp(close.toEpochMilli())
                .closePrice(closePrice)
                .build();
        testCreate(candleStick);
        candleStick.setClosePrice(random.nextDouble());
        candleStick.update();
        Optional<CandleStick> candleStickOptional = CandleStick.findByOpenTimestampAndIsin(open.toEpochMilli(), isin);
        Assertions.assertTrue(candleStickOptional.isPresent());
        CandleStick updatedCandlestick = candleStickOptional.get();
        Assertions.assertNotEquals(closePrice, updatedCandlestick.getClosePrice());

    }

    @Test
    @DisplayName("Should find candlesticks in given range")
    void findByIsinAndRange() {
        String isin = UUID.randomUUID().toString();
        Instant close = currentMinuteTimestamp();
        Instant open = close.minusSeconds(60);
        CandleStick candleStick1 = CandleStick.builder()
                .isin(isin)
                .openTimestamp(open.toEpochMilli())
                .closeTimestamp(close.toEpochMilli())
                .build();
        CandleStick candleStick2 = CandleStick.builder()
                .isin(UUID.randomUUID().toString())
                .openTimestamp(open.toEpochMilli())
                .closeTimestamp(close.toEpochMilli())
                .build();
        testCreate(candleStick1);
        testCreate(candleStick2);
        List<CandleStick> candleSticksInRange = CandleStick.findByIsinAndRange(isin, open.toEpochMilli(), close.toEpochMilli());
        Assertions.assertFalse(candleSticksInRange.isEmpty());
        Assertions.assertTrue(candleSticksInRange.stream().anyMatch(candleStick -> candleStick.getIsin().equals(candleStick1.getIsin())));
        List<CandleStick> candleSticksWrong = CandleStick.findByIsinAndRange(isin, open.plusSeconds(1000000L), close.plusSeconds(10000000L));
        Assertions.assertTrue(candleSticksWrong.isEmpty());
    }

    @Test
    @DisplayName("Should find candlesticks by open timestamp and isin")
    void findByOpenTimestamp() {
        Instant close = currentMinuteTimestamp();
        Instant open = close.minusSeconds(60);

        CandleStick candleStick = CandleStick.builder().isin(UUID.randomUUID().toString()).openTimestamp(open.toEpochMilli()).closeTimestamp(close.toEpochMilli()).build();
        candleStick.persist();
        Optional<CandleStick> candleStickOptional = CandleStick.findByOpenTimestampAndIsin(open.toEpochMilli(), candleStick.getIsin());
        Assertions.assertTrue(candleStickOptional.isPresent());
        CandleStick persistedCandlestick = candleStickOptional.get();
        assertCandlestick(candleStick, persistedCandlestick);
        Optional<CandleStick> candleStickOptional2 = CandleStick.findByOpenTimestampAndIsin(open.toEpochMilli(), candleStick.getIsin());
        Assertions.assertTrue(candleStickOptional2.isPresent());
        assertCandlestick(candleStick, candleStickOptional2.get());
        Optional<CandleStick> optionalCandleStick = CandleStick.findByOpenTimestampAndIsin(Instant.now(), candleStick.getIsin());
        Assertions.assertTrue(optionalCandleStick.isEmpty());

    }

    @Test
    @DisplayName("Should persist a candlestick")
    void shouldPersistCandlestick() {
        Instant currentMinuteTimestamp = currentMinuteTimestamp();
        Instant minusMinute = currentMinuteTimestamp.minusSeconds(60);
        double openPrice = RANDOM.nextDouble();
        double closePrice = RANDOM.nextDouble();
        double lowPrice = RANDOM.nextDouble();
        double highPrice = RANDOM.nextDouble();
        CandleStick candleStick = CandleStick.builder()
                .openPrice(openPrice)
                .isin(UUID.randomUUID().toString())
                .closePrice(closePrice)
                .lowPrice(lowPrice)
                .highPrice(highPrice)
                .openTimestamp(minusMinute.toEpochMilli())
                .closeTimestamp(currentMinuteTimestamp.toEpochMilli())
                .build();
        testCreate(candleStick);

    }

    @AfterEach
    public void cleanUp() {
        CandleStick.deleteAll();
        Assertions.assertEquals(0, CandleStick.count());
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
        Assertions.assertEquals(first.getIsin(), second.getIsin());
        Assertions.assertEquals(first.getOpenPrice(), second.getOpenPrice());
        Assertions.assertEquals(first.getClosePrice(), second.getClosePrice());
        Assertions.assertEquals(first.getHighPrice(), second.getHighPrice());
        Assertions.assertEquals(first.getLowPrice(), second.getLowPrice());
        Assertions.assertEquals(first.getOpenTimestamp(), second.getOpenTimestamp());
        Assertions.assertEquals(first.getCloseTimestamp(), second.getCloseTimestamp());
    }

}