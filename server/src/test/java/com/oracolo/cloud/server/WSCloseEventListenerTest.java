package com.oracolo.cloud.server;

import com.oracolo.cloud.entities.CandleStick;
import com.oracolo.cloud.entities.Instrument;
import com.oracolo.cloud.entities.Quote;
import com.oracolo.cloud.events.InstrumentData;
import com.oracolo.cloud.events.InstrumentEvent;
import com.oracolo.cloud.events.QuoteData;
import com.oracolo.cloud.events.QuoteEvent;
import com.oracolo.cloud.streamhandler.StreamHandler;
import io.quarkus.test.junit.QuarkusTest;
import liquibase.pro.packaged.Q;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.websocket.CloseReason;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class WSCloseEventListenerTest extends BaseGrindTest {

    @Inject
    WSCloseEventListener wsCloseEventListener;
    @Inject
    CandleStickManager candleStickManager;

    @Test
    void onClose() {
        addStreamData(Instant.now().minusSeconds(60));
        candleStickManager.grindData();
        Assertions.assertFalse(Quote.listAll().isEmpty());
        Assertions.assertFalse(Instrument.listAll().isEmpty());
        Assertions.assertFalse(CandleStick.listAll().isEmpty());
        CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, "ksjudgbfkij");
        wsCloseEventListener.onClose(closeReason);
        Assertions.assertEquals(0, Quote.count());
        Assertions.assertEquals(0, Instrument.count());
        Assertions.assertEquals(0, CandleStick.count());


    }
}