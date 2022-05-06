package com.oracolo.cloud.entities.sql;

import com.oracolo.cloud.entities.CurrentTimeStampUtils;
import com.oracolo.cloud.entities.Quote;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import java.time.*;
import java.util.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.Random.class)
class PersistenceQuoteTest {

    private static final Random RANDOM = new Random();

    @Test
    @DisplayName("Should persist an quote")
    void shouldPersistQuote() {
        Quote quote = Quote.builder().isin(UUID.randomUUID().toString())
                .timestamp(Instant.now().toEpochMilli()).price(RANDOM.nextDouble()).build();
        testCreate(quote);
    }

    @Test
    @DisplayName("Find quotes in given range")
    public void shouldFindQuotesInGivenRange() {
        Instant quote1Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 24), ZoneId.systemDefault()).toInstant();
        Instant quote2Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 24), ZoneId.systemDefault()).toInstant();
        Instant quote3Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 26), ZoneId.systemDefault()).toInstant();
        Instant quote4Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 27), ZoneId.systemDefault()).toInstant();
        Instant quote5Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 28), ZoneId.systemDefault()).toInstant();
        Quote quote1 = Quote.builder().isin(UUID.randomUUID().toString())
                .timestamp(quote1Time.toEpochMilli()).price(RANDOM.nextDouble()).build();
        Quote quote2 = Quote.builder().isin(UUID.randomUUID().toString())
                .timestamp(quote2Time.toEpochMilli()).price(RANDOM.nextDouble()).build();
        Quote quote3 = Quote.builder().isin(UUID.randomUUID().toString())
                .timestamp(quote3Time.toEpochMilli()).price(RANDOM.nextDouble()).build();
        Quote quote4 = Quote.builder().isin(UUID.randomUUID().toString())
                .timestamp(quote4Time.toEpochMilli()).price(RANDOM.nextDouble()).build();
        Quote quote5 = Quote.builder().isin(UUID.randomUUID().toString())
                .timestamp(quote5Time.toEpochMilli()).price(RANDOM.nextDouble()).build();
        testCreate(quote1);
        testCreate(quote2);
        testCreate(quote3);
        testCreate(quote4);
        testCreate(quote5);
        List<Quote> quotesInRange = Quote.findInRange(quote1Time.toEpochMilli(), quote5Time.toEpochMilli());
        Assertions.assertEquals(5, quotesInRange.size());
        assertCollection(List.of(quote1.getIsin(), quote2.getIsin(), quote3.getIsin(), quote4.getIsin(), quote5.getIsin()), quotesInRange);
        List<Quote> quotesInRange2 = Quote.findInRange(quote1Time.toEpochMilli(), quote1Time.toEpochMilli());
        Assertions.assertEquals(2, quotesInRange2.size());
        assertCollection(List.of(quote1.getIsin(), quote2.getIsin()), quotesInRange2);
        List<Quote> quotesInRange3 = Quote.findInRange(quote3Time.toEpochMilli(), quote5Time.toEpochMilli());
        Assertions.assertEquals(3, quotesInRange3.size());
        assertCollection(List.of(quote3.getIsin(), quote4.getIsin(), quote5.getIsin()), quotesInRange3);
    }

    @Test
    @DisplayName("Should delete by isin and timestamp")
    public void shouldDeleteByIsinTimestamp() {
        Instant quote1Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 26), ZoneId.systemDefault()).toInstant();
        Instant quote2Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 27), ZoneId.systemDefault()).toInstant();
        Instant quote3Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 28), ZoneId.systemDefault()).toInstant();
        String isin = UUID.randomUUID().toString();
        Quote quote1 = Quote.builder().isin(isin).timestamp(quote1Time.toEpochMilli()).price(RANDOM.nextDouble()).build();
        Quote quote2 = Quote.builder().isin(isin)
                .timestamp(quote2Time.toEpochMilli()).price(RANDOM.nextDouble()).build();
        Quote quote3 = Quote.builder().isin(UUID.randomUUID().toString())
                .timestamp(quote3Time.toEpochMilli()).price(RANDOM.nextDouble()).build();
        testCreate(quote1);
        testCreate(quote2);
        testCreate(quote3);
        Quote.deleteByIsinAndTimestamp(isin, quote1Time.toEpochMilli());
        List<Quote> quotes = Quote.listAll();
        Assertions.assertEquals(2, quotes.size());
        assertCollection(List.of(quote2.getIsin(), quote3.getIsin()), quotes);
        //add back
        testCreate(quote1);
        Quote.deleteByIsinAndTimestamp(isin, quote3Time.toEpochMilli());
        List<Quote> only3Quote = Quote.listAll();
        Assertions.assertEquals(1, only3Quote.size());
        assertCollection(Collections.singleton(quote3.getIsin()), only3Quote);
    }

    @Test
    @DisplayName("Should find all quotes in given timestamp range with isin in the given ones")
    public void shouldFindAllQuotesByIsinsTimestampRange() {
        Instant quote1Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 26), ZoneId.systemDefault()).toInstant();
        Instant quote2Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 27), ZoneId.systemDefault()).toInstant();
        Instant quote3Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 28), ZoneId.systemDefault()).toInstant();
        String isin = UUID.randomUUID().toString();
        Quote quote1 = Quote.builder().isin(isin).timestamp(quote1Time.toEpochMilli()).price(RANDOM.nextDouble()).build();
        Quote quote2 = Quote.builder().isin(isin)
                .timestamp(quote2Time.toEpochMilli()).price(RANDOM.nextDouble()).build();
        Quote quote3 = Quote.builder().isin(UUID.randomUUID().toString())
                .timestamp(quote3Time.toEpochMilli()).price(RANDOM.nextDouble()).build();
        testCreate(quote1);
        testCreate(quote2);
        testCreate(quote3);
        List<Quote> quotes = Quote.findByIsinsInRange(List.of(quote1.getIsin(), quote3.getIsin(), quote2.getIsin()), quote1Time.toEpochMilli(), quote3Time.toEpochMilli());
        Assertions.assertEquals(2, quotes.size());
        assertCollection(List.of(quote2.getIsin(), quote3.getIsin(), quote1.getIsin()), quotes);
        List<Quote> filterOutIsins = Quote.findByIsinsInRange(List.of(quote2.getIsin()), quote1Time.toEpochMilli(), quote3Time.toEpochMilli());
        Assertions.assertEquals(2, filterOutIsins.size());
        assertCollection(List.of(quote2.getIsin()),filterOutIsins);
    }

    @AfterEach
    public void cleanUp() {
        Quote.deleteAll();
        Assertions.assertEquals(0, Quote.count());
    }

    private static Quote testCreate(Quote quote) {
        quote.persist();
        Optional<Quote> quoteOptional = Quote.findByIdOptional(quote.id);
        Assertions.assertTrue(quoteOptional.isPresent());
        Quote persistedQuote = quoteOptional.get();
        Assertions.assertNotNull(persistedQuote);
        assertQuote(quote, persistedQuote);
        return persistedQuote;
    }

    private static void assertQuote(Quote first, Quote second) {
        Assertions.assertEquals(first.id, second.id);
        Assertions.assertEquals(first.getIsin(), second.getIsin());
        Assertions.assertEquals(first.getPrice(), second.getPrice());
        Assertions.assertEquals(first.getTimestamp(), second.getTimestamp());
    }

    /**
     * Assert that all given {@link Quote}s have isin equals to the given collection of isins
     *
     * @param isins  a {@link Collection} of isin
     * @param quotes a {@link Collection} of {@link Quote}
     */
    private static void assertCollection(Collection<String> isins, Collection<Quote> quotes) {
        Assertions.assertTrue(quotes.stream().allMatch(quote -> isins.contains(quote.getIsin())));
    }

}