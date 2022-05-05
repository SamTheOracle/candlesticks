package com.oracolo.cloud.server;

import static com.oracolo.cloud.entities.CurrentTimeStampUtils.currentMinuteTimestamp;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.oracolo.cloud.entities.InstrumentStream;
import com.oracolo.cloud.entities.QuoteStream;
import com.oracolo.cloud.entities.sql.CandleStick;
import com.oracolo.cloud.entities.sql.Instrument;
import com.oracolo.cloud.events.InstrumentData;
import com.oracolo.cloud.events.InstrumentEvent;
import com.oracolo.cloud.events.InstrumentEventType;
import com.oracolo.cloud.events.QuoteData;
import com.oracolo.cloud.events.QuoteEvent;
import com.oracolo.cloud.streamhandler.StreamHandler;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestMethodOrder(MethodOrderer.Random.class)
class CandleStickServiceTest {
	private static final Random RANDOM = new Random();
	private static final int MAX_NUMBER_INSTRUMENT = Math.abs(RANDOM.nextInt(70));
	private static final int MAX_QUOTES = RANDOM.nextInt(150);
	private static final List<String> ISINS = IntStream.range(0, MAX_NUMBER_INSTRUMENT).mapToObj(
			value -> UUID.randomUUID().toString()).collect(Collectors.toUnmodifiableList());

	@Inject
	CandleStickManager candleStickManager;

	@Inject
	StreamHandler streamHandler;

	@Test
	@TestTransaction
	void getCandlesticksByIsin() {
		candleStickManager.grindData();
		Instant close = currentMinuteTimestamp();
		Instant open = close.minusSeconds(60);
		String isin = ISINS.get(RANDOM.nextInt(MAX_NUMBER_INSTRUMENT));
		Optional<CandleStick> candleStickOptional = CandleStick.findByOpenTimestamp(open,isin);
		Assertions.assertTrue(candleStickOptional.isPresent());
		List<CandleStick> candleSticks = candleStickManager.getCandlesticksByIsin(isin);
		Assertions.assertFalse(candleSticks.isEmpty());
		Assertions.assertTrue(candleSticks.stream().allMatch(
				candleStick -> candleStick.getOpenTimestamp().equals(open) && candleStick.getCloseTimestamp().equals(close)));
		assertEquals(1, candleSticks.size());
	}

	@Test
	@TestTransaction
	void grindData() {
		candleStickManager.grindData();
		List<CandleStick> candleSticks = CandleStick.listAll();
		Assertions.assertFalse(candleSticks.isEmpty());
		Assertions.assertEquals(MAX_NUMBER_INSTRUMENT, candleSticks.size());
	}

	@BeforeEach
	public void addStreamData() {
		ISINS.forEach(isin -> {
			InstrumentEvent instrumentEvent = new InstrumentEvent();
			instrumentEvent.type = InstrumentEventType.ADD;
			instrumentEvent.data = new InstrumentData();
			instrumentEvent.data.description = "";
			instrumentEvent.data.isin = isin;
			streamHandler.handleInstrumentEvent(instrumentEvent);
		});
		List<InstrumentStream> instrumentStreams = InstrumentStream.listAll();
		Assertions.assertEquals(MAX_NUMBER_INSTRUMENT, InstrumentStream.count());
		IntStream.range(0, MAX_QUOTES).forEach(value -> {
			int randomIndex = RANDOM.nextInt(MAX_NUMBER_INSTRUMENT);
			String isin = instrumentStreams.get(randomIndex).getIsin();
			double price = RANDOM.nextDouble();
			QuoteEvent quoteEvent = new QuoteEvent();
			quoteEvent.data = new QuoteData();
			quoteEvent.data.isin = isin;
			quoteEvent.data.price = price;
			streamHandler.handleQuoteEvent(quoteEvent);
		});

		Assertions.assertEquals(MAX_QUOTES, QuoteStream.count());
	}

	@AfterEach
	void cleanUp() {
		QuarkusTransaction.run(()-> {
			CandleStick.deleteAll();
			Instrument.deleteAll();
			assertEquals(0, Instrument.count());
			assertEquals(0, CandleStick.count());
		});

	}

	@AfterEach
	void cleanUpMongoData() {
		QuoteStream.deleteAll();
		InstrumentStream.deleteAll();
		Assertions.assertEquals(0, QuoteStream.count());
		Assertions.assertEquals(0, InstrumentStream.count());
	}
}