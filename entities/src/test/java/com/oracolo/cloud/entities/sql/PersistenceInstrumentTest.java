package com.oracolo.cloud.entities.sql;

import com.oracolo.cloud.entities.Instrument;
import com.oracolo.cloud.entities.Status;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import java.time.*;
import java.util.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.Random.class)
class PersistenceInstrumentTest {


    @Test
    @DisplayName("Should persist an instrument")
    void shouldPersistInstrument() {
        Instrument instrumentAdded = Instrument.builder().isin(UUID.randomUUID().toString()).description(UUID.randomUUID().toString())
                .timestamp(Instant.now().toEpochMilli()).build();
        Instrument instrumentDeleted = Instrument.builder().isin(UUID.randomUUID().toString()).description(UUID.randomUUID().toString())
                .timestamp(Instant.now().toEpochMilli()).build();
        testCreate(instrumentAdded);
        testCreate(instrumentDeleted);
        Optional<Instrument> instrumentOptional = Instrument.findByIsin(instrumentAdded.getIsin());
        Assertions.assertTrue(instrumentOptional.isPresent());
        assertInstrument(instrumentAdded,instrumentOptional.get());
    }

    @Test
    @DisplayName("Find instruments in given range")
    public void shouldFindInstrumentsInGivenRange() {
        Instant instrument1Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 24), ZoneId.systemDefault()).toInstant();
        Instant instrument2Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 24), ZoneId.systemDefault()).toInstant();
        Instant instrument3Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 26), ZoneId.systemDefault()).toInstant();
        Instant instrument4Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 27), ZoneId.systemDefault()).toInstant();
        Instant instrument5Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 28), ZoneId.systemDefault()).toInstant();
        Instrument instrument1 = Instrument.builder().isin(UUID.randomUUID().toString()).description(UUID.randomUUID().toString())
                .timestamp(instrument1Time.toEpochMilli()).build();
        Instrument instrument2 = Instrument.builder().isin(UUID.randomUUID().toString()).description(UUID.randomUUID().toString())
                .timestamp(instrument2Time.toEpochMilli()).build();
        Instrument instrument3 = Instrument.builder().isin(UUID.randomUUID().toString()).description(UUID.randomUUID().toString())
                .timestamp(instrument3Time.toEpochMilli()).build();
        Instrument instrument4 = Instrument.builder().isin(UUID.randomUUID().toString()).description(UUID.randomUUID().toString())
                .timestamp(instrument4Time.toEpochMilli()).build();
        Instrument instrument5 = Instrument.builder().isin(UUID.randomUUID().toString()).description(UUID.randomUUID().toString())
                .timestamp(instrument5Time.toEpochMilli()).build();
        testCreate(instrument1);
        testCreate(instrument2);
        testCreate(instrument3);
        testCreate(instrument4);
        testCreate(instrument5);
        List<Instrument> instrumentsInRange = Instrument.findByRange(instrument1Time.toEpochMilli(), instrument5Time.toEpochMilli());
        Assertions.assertEquals(5, instrumentsInRange.size());
        assertCollection(List.of(instrument1.getIsin(), instrument2.getIsin(), instrument3.getIsin(), instrument4.getIsin(), instrument5.getIsin()), instrumentsInRange);
        List<Instrument> instrumentsInRange2 = Instrument.findByRange(instrument1Time.toEpochMilli(), instrument1Time.toEpochMilli());
        Assertions.assertEquals(2, instrumentsInRange2.size());
        assertCollection(List.of(instrument1.getIsin(), instrument2.getIsin()), instrumentsInRange2);
        List<Instrument> instrumentsInRange3 = Instrument.findByRange(instrument3Time.toEpochMilli(), instrument5Time.toEpochMilli());
        Assertions.assertEquals(3, instrumentsInRange3.size());
        assertCollection(List.of(instrument3.getIsin(), instrument4.getIsin(), instrument5.getIsin()), instrumentsInRange3);
    }

    @Test
    @DisplayName("Should delete by isin and timestamp")
    public void shouldDeleteByIsinTimestamp() {
        Instant instrument1Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 26), ZoneId.systemDefault()).toInstant();
        Instant instrument2Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 27), ZoneId.systemDefault()).toInstant();
        Instant instrument3Time = ZonedDateTime.of(LocalDate.now(), LocalTime.of(10, 28), ZoneId.systemDefault()).toInstant();
        String isin = UUID.randomUUID().toString();
        Instrument instrument1 = Instrument.builder().isin(isin).description(UUID.randomUUID().toString())
                .timestamp(instrument1Time.toEpochMilli()).build();
        Instrument instrument2 = Instrument.builder().isin(isin).description(UUID.randomUUID().toString())
                .timestamp(instrument2Time.toEpochMilli()).build();
        Instrument instrument3 = Instrument.builder().isin(UUID.randomUUID().toString()).description(UUID.randomUUID().toString())
                .timestamp(instrument3Time.toEpochMilli()).build();
        testCreate(instrument1);
        testCreate(instrument2);
        testCreate(instrument3);
        Instrument.deleteByIsinTimestamp(isin,instrument1Time.toEpochMilli());
        List<Instrument> instruments = Instrument.listAll();
        Assertions.assertEquals(2,instruments.size());
        assertCollection(List.of(instrument2.getIsin(),instrument3.getIsin()),instruments);
        //add back
        testCreate(instrument1);
        Instrument.deleteByIsinTimestamp(isin,instrument3Time.toEpochMilli());
        List<Instrument> only3Instrument = Instrument.listAll();
        Assertions.assertEquals(1,only3Instrument.size());
        assertCollection(Collections.singleton(instrument3.getIsin()),only3Instrument);

    }

    @AfterEach
    public void cleanUp() {
        Instrument.deleteAll();
        Assertions.assertEquals(0, Instrument.count());
    }

    private static Instrument testCreate(Instrument instrument) {
        instrument.persist();
        Optional<Instrument> instrumentOptional = Instrument.findByIdOptional(instrument.id);
        Assertions.assertTrue(instrumentOptional.isPresent());
        Instrument persistedInstrument = instrumentOptional.get();
        Assertions.assertNotNull(persistedInstrument);
        assertInstrument(instrument, persistedInstrument);
        return persistedInstrument;
    }

    private static void assertInstrument(Instrument first, Instrument second) {
        Assertions.assertEquals(first.id, second.id);
        Assertions.assertEquals(first.getIsin(), second.getIsin());
        Assertions.assertEquals(first.getDescription(), second.getDescription());
        Assertions.assertEquals(first.getTimestamp(), second.getTimestamp());
    }

    /**
     * Assert that all given {@link Instrument}s have isin equals to the given collection of isins
     *
     * @param isins       a {@link Collection} of isin
     * @param instruments a {@link Collection} of {@link Instrument}
     */
    private static void assertCollection(Collection<String> isins, Collection<Instrument> instruments) {
        Assertions.assertTrue(instruments.stream().allMatch(instrument -> isins.contains(instrument.getIsin())));
    }

}