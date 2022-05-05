package com.oracolo.cloud.entities;


import java.time.Instant;
import java.util.List;

import com.oracolo.cloud.events.CandlestickInstrument;
import com.oracolo.cloud.events.InstrumentEventType;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;

@MongoEntity(collection = "instrument_data", database = "streamevents")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@Getter
@Setter
public class InstrumentStream extends PanacheMongoEntity {

    private String description;
    private long timestamp;
    private String isin;
    private Status status;

    public static List<InstrumentStream> findInstrumentsByRange(long from, long to) {
        return list("timestamp >= ?1 and timestamp <= ?2", from, to);
    }


    public static InstrumentStream from(CandlestickInstrument instrumentEvent) {
        String isin = instrumentEvent.isin();
        Status status;
        InstrumentEventType instrumentEventType = instrumentEvent.type();
        if (instrumentEventType == InstrumentEventType.ADD) {
            status = Status.ADDED;
        } else {
            status = Status.DELETED;
        }
        String description = instrumentEvent.description();
        return InstrumentStream.builder().isin(isin).status(status).description(description).timestamp(Instant.now().toEpochMilli()).build();
    }
}
