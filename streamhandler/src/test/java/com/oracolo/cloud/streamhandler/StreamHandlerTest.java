package com.oracolo.cloud.streamhandler;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.inject.Inject;

import com.oracolo.cloud.streamhandler.testdata.InstrumentEventTest;
import com.oracolo.cloud.streamhandler.testdata.QuoteEventTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.oracolo.cloud.entities.Instrument;
import com.oracolo.cloud.entities.Quote;
import com.oracolo.cloud.events.CandlestickInstrument;
import com.oracolo.cloud.events.CandlestickQuote;
import com.oracolo.cloud.events.InstrumentEventType;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestMethodOrder(MethodOrderer.Random.class)
public class StreamHandlerTest {

	@Inject
	StreamHandler streamHandler;

	@Test
	@DisplayName("No data should be present")
	public void noDataShouldBePresent() {
		Instant now = Instant.now();
		Instant from = now.minusSeconds(120);
		List<QuotedInstrument> quotedInstruments = streamHandler.fetchStream(now, from);
		Assertions.assertTrue(quotedInstruments.isEmpty());
	}

	@Test
	@DisplayName("Should add a new instrument event")
	public void shouldAddNewInstrument() {
		Instant now = Instant.now();
		String isin = UUID.randomUUID().toString();
		String description = "not the best, really";
		InstrumentEventTest instrumentEventTest = new InstrumentEventTest(isin, description, InstrumentEventType.ADD, Instant.now().toEpochMilli());
		streamHandler.handleInstrumentEvent(instrumentEventTest);
		Instant from = now.minusSeconds(120);
		List<QuotedInstrument> quotedInstruments = streamHandler.fetchStream(from, now);
		Assertions.assertFalse(quotedInstruments.isEmpty());
		Assertions.assertEquals(1, quotedInstruments.size());
		Assertions.assertEquals(instrumentEventTest.isin(), quotedInstruments.get(0).isin());
		Assertions.assertEquals(instrumentEventTest.description(), quotedInstruments.get(0).description());
		Assertions.assertEquals(instrumentEventTest.type(), quotedInstruments.get(0).type());
	}

	@Test
	@DisplayName("Should add a new quote event")
	public void shouldAddANewQuoteEvent() {
		String isin = UUID.randomUUID().toString();
		Instant now = Instant.now();
		CandlestickInstrument candlestickInstrument = testInstrumentCreate(isin, InstrumentEventType.ADD);
		double price = 12.45;
		testQuoteCreation(isin, price);
		Instant oneMinuteLater = now.plusSeconds(60);
		List<QuotedInstrument> quotedInstruments = streamHandler.fetchStream(now,oneMinuteLater);
		Assertions.assertNotNull(quotedInstruments);
		Assertions.assertFalse(quotedInstruments.isEmpty());
		Assertions.assertEquals(1, quotedInstruments.size());
		QuotedInstrument quotedInstrument = quotedInstruments.get(0);
		Assertions.assertEquals(candlestickInstrument.isin(), quotedInstrument.isin());
		List<CandlestickQuote> quotes = quotedInstrument.quotes();
		Assertions.assertFalse(quotes.isEmpty());
		Assertions.assertEquals(1, quotes.size());
		long ts = quotes.get(0).timestamp();
		long from = now.toEpochMilli();
		long to = oneMinuteLater.toEpochMilli();
		Assertions.assertTrue(ts >= from && ts <= to);
		Assertions.assertEquals(price, quotes.get(0).price());
	}

	@Test
	@DisplayName("Should fetch instruments with no data")
	public void shouldFetchInstrumentWithNoData() {
		String isin1 = UUID.randomUUID().toString();
		CandlestickInstrument one = testInstrumentCreate(isin1, InstrumentEventType.ADD);
		String isin2 = UUID.randomUUID().toString();
		CandlestickInstrument two = testInstrumentCreate(isin2, InstrumentEventType.ADD);
		Instant now = Instant.now();
		Instant from = now.minusSeconds(120);
		List<QuotedInstrument> quotedInstruments = streamHandler.fetchStream(from, now);
		Assertions.assertFalse(quotedInstruments.isEmpty());
		Assertions.assertEquals(2, quotedInstruments.size());
		Assertions.assertTrue(quotedInstruments.stream().anyMatch(quotedInstrument -> isin1.equals(quotedInstrument.isin())));
		Assertions.assertTrue(quotedInstruments.stream().anyMatch(quotedInstrument -> isin2.equals(quotedInstrument.isin())));
		Assertions.assertEquals(0, quotedInstruments.stream().mapToLong(quotedInstrument -> quotedInstrument.quotes().size()).sum());
	}

	@Test
	@DisplayName("Should delete data when delete is coming")
	public void shouldDeleteDataWhenDeleteIsComing() {
		String isin = UUID.randomUUID().toString();
		testInstrumentCreate(isin, InstrumentEventType.ADD);
		testQuoteCreation(isin,123.2);
		testQuoteCreation(isin,78.2);
		testInstrumentCreate(isin, InstrumentEventType.DELETE);
	}

	@Test
	@DisplayName("Should handle back insert of instrument correctly")
	public void shouldHandleBackInsert(){
		String isin = UUID.randomUUID().toString();
		testInstrumentCreate(isin,InstrumentEventType.ADD);
		Random random = new Random();
		double price1 = random.nextDouble();
		double price2 = random.nextDouble();
		testQuoteCreation(isin,price1);
		testQuoteCreation(isin, price2);
		testInstrumentCreate(isin, InstrumentEventType.DELETE);
		testInstrumentCreate(isin, InstrumentEventType.ADD);
		double price3 = random.nextDouble();
		double price4 = random.nextDouble();
		testQuoteCreation(isin,price3);
		testQuoteCreation(isin,price4);
		Instant now = Instant.now();
		Instant from = now.minusSeconds(120);
		List<QuotedInstrument> quotedInstruments = streamHandler.fetchStream(from, now);
		Assertions.assertFalse(quotedInstruments.isEmpty());
		Assertions.assertEquals(1,quotedInstruments.size());
		QuotedInstrument quotedInstrument = quotedInstruments.get(0);
		List<CandlestickQuote> quotes = quotedInstrument.quotes();
		Assertions.assertEquals(4, quotes.size());
	}

	@BeforeEach
	public void shouldDelete() {
		Instrument.deleteAll();
		Quote.deleteAll();
		Assertions.assertTrue(Quote.listAll().isEmpty());
		Assertions.assertTrue(Instrument.listAll().isEmpty());
	}

	private void testQuoteCreation(String isin, double price) {
		QuoteEventTest quoteEventTest = new QuoteEventTest(isin, price, Instant.now().toEpochMilli());
		streamHandler.handleQuoteEvent(quoteEventTest);
	}

	private CandlestickInstrument testInstrumentCreate(String isin, InstrumentEventType type) {
		InstrumentEventTest instrumentEventTest = new InstrumentEventTest(isin, UUID.randomUUID().toString(), type,
				Instant.now().toEpochMilli());
		streamHandler.handleInstrumentEvent(instrumentEventTest);
		Instant now = Instant.now();
		Instant from = now.minusSeconds(120);
		List<QuotedInstrument> quotedInstruments = streamHandler.fetchStream(from, now);
		if(type==InstrumentEventType.ADD){
			Assertions.assertFalse(quotedInstruments.isEmpty());
		}else{
			Assertions.assertTrue(quotedInstruments.isEmpty());
		}
		return instrumentEventTest;
	}
}
