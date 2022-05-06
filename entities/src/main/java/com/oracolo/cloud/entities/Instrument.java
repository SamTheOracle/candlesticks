package com.oracolo.cloud.entities;


import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.oracolo.cloud.events.CandlestickInstrument;
import com.oracolo.cloud.events.InstrumentEventType;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.Document;

@MongoEntity(collection = "instrument_data", database = "streamevents")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@Getter
@Setter
public class Instrument extends PanacheMongoEntity {

    private String description;
    private long timestamp;
    private String isin;

    public static Instrument from(CandlestickInstrument instrumentEvent) {
        String isin = instrumentEvent.isin();
        String description = instrumentEvent.description();
        return Instrument.builder().isin(isin).description(description).timestamp(Instant.now().toEpochMilli()).build();
    }

    public static void deleteByIsinTimestamp(String isin, long timestamp) {
        delete("isin=?1 and timestamp<=?2", isin, timestamp);
    }

    public static List<Instrument> findByRange(long from, long to) {
        String query = String.format("{'timestamp':{'$gte':%d,'$lte':%d}}", from, to);
        Document document = Document.parse(query);
        return find(document).list();
    }

    public static Optional<Instrument> findByIsin(String isin) {
        return find("isin",isin).firstResultOptional();
    }
}
