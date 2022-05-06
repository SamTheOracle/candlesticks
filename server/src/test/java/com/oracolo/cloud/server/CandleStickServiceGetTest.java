package com.oracolo.cloud.server;

import com.oracolo.cloud.entities.CandleStick;
import com.oracolo.cloud.entities.Quote;
import com.oracolo.cloud.server.dto.CandleStickDto;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestMethodOrder(MethodOrderer.Random.class)
public class CandleStickServiceGetTest extends BaseGrindTest{



    @Inject
    CandleStickManager candleStickManager;

    @Inject
    CandleStickConverter converter;

    @Test
    @DisplayName("Get candlesticks by isin with quotes every minute in the last 30 minutes")
    void getCandlesticksByIsin() {
        addStreamData(Instant.now().minusSeconds(60));
        candleStickManager.grindData();
        String isin = ISINS.get(RANDOM.nextInt(MAX_NUMBER_INSTRUMENT));
        List<CandleStick> candleSticks = candleStickManager.getCandlesticksByIsin(isin);
        Assertions.assertTrue(candleSticks.size() > 1);
        Assertions.assertTrue(candleSticks.stream().allMatch(candleStick -> {
            List<Quote> quotes = Quote.findByIsinsInRange(Collections.singleton(isin), candleStick.getOpenTimestamp(), candleStick.getCloseTimestamp());
            double high = quotes.parallelStream().mapToDouble(Quote::price).max().orElse(0.0);
            double low = quotes.parallelStream().mapToDouble(Quote::price).min().orElse(0.0);
            double openP = quotes.parallelStream().reduce((quote1, quote2) -> quote1.timestamp() < quote2.timestamp() ? quote1 : quote2)
                    .map(Quote::price).orElse(0.0);
            double closeP = quotes.parallelStream().reduce((quote1, quote2) -> quote1.timestamp() > quote2.timestamp() ? quote1 : quote2)
                    .map(Quote::price).orElse(0.0);
            return candleStick.getClosePrice() == closeP && candleStick.getOpenPrice() == openP && candleStick.getHighPrice() == high && candleStick.getLowPrice() == low;
        }));
        List<CandleStickDto> candleStickDtos = converter.from(candleSticks);
        assertEquals(candleStickDtos.size(), candleSticks.size());

    }




}
