package com.oracolo.cloud.server;

import com.oracolo.cloud.entities.InstrumentStream;
import com.oracolo.cloud.entities.QuoteStream;
import com.oracolo.cloud.entities.sql.CandleStick;
import com.oracolo.cloud.entities.sql.Instrument;
import com.oracolo.cloud.events.*;
import com.oracolo.cloud.streamhandler.StreamHandler;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import javax.inject.Inject;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.Random.class)
class CandleStickServiceTest {

    @Inject
    CandleStickService candleStickService;

    @Inject
    StreamHandler streamHandler;

    @Test
    @TestTransaction
    void getCandlesticksByIsin() {
    }

    @Test
    @TestTransaction
    void grindData() {

    }

    @BeforeEach
    @TestTransaction
    public void addData(){
        IntStream.range(0,11).forEach(value -> {
            InstrumentEvent instrumentEvent = new InstrumentEvent();
            instrumentEvent.type = InstrumentEventType.ADD;
            instrumentEvent.data = new InstrumentData();
            instrumentEvent.data.description = "";
            instrumentEvent.data.isin = UUID.randomUUID().toString();
            streamHandler.handleInstrumentEvent(instrumentEvent);
        });
        List<InstrumentStream> instrumentStreams = InstrumentStream.listAll();
        Random boundedRandom = new Random();
        IntStream.range(0,100).forEach(value -> {
            int randomIndex = boundedRandom.nextInt(9);
            String isin = instrumentStreams.get(randomIndex).getIsin();
            double price = boundedRandom.nextDouble();
            QuoteEvent quoteEvent = new QuoteEvent();
            quoteEvent.data = new QuoteData();
            quoteEvent.data.isin = isin;
            quoteEvent.data.price = price;
            streamHandler.handleQuoteEvent(quoteEvent);
        });

        Assertions.assertEquals(10, instrumentStreams.size());
        Assertions.assertEquals(100, QuoteStream.listAll().size());
    }

    @AfterEach
    public void cleanUp(){
        assertEquals(0, Instrument.count());
        assertEquals(0, CandleStick.count());
    }
}