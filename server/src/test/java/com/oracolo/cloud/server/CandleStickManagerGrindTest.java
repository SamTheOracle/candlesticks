package com.oracolo.cloud.server;

import com.oracolo.cloud.entities.CandleStick;
import com.oracolo.cloud.entities.Instrument;
import com.oracolo.cloud.entities.Quote;
import com.oracolo.cloud.events.*;
import com.oracolo.cloud.streamhandler.StreamHandler;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.oracolo.cloud.entities.CurrentTimeStampUtils.currentMinuteTimestamp;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestMethodOrder(MethodOrderer.Random.class)
class CandleStickManagerGrindTest extends BaseGrindTest{


    @Inject
    CandleStickManager candleStickManager;

    @Test
    void grindData() {
        addStreamData(Instant.now().minusSeconds(60));
        candleStickManager.grindData();
        List<CandleStick> candleSticks = CandleStick.listAll();
        Assertions.assertFalse(candleSticks.isEmpty());
        Assertions.assertEquals(MAX_NUMBER_INSTRUMENT, candleSticks.stream().map(CandleStick::getIsin).collect(Collectors.toUnmodifiableSet()).size());
    }

}