package com.oracolo.cloud.entities;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.oracolo.cloud.events.CandleStickQuote;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.Document;

/**
 * A {@link Quote} is the price of a specific {@link Instrument} in a given moment
 */
@MongoEntity(collection = "quote_data", database = "streamevents")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@Getter
@Setter
public class Quote extends PanacheMongoEntity implements CandleStickQuote {
    private String isin;
    private double price;
    private String type;
    private long timestamp;

    public static List<Quote> findInRange(long from, long to) {
        String query = String.format("{'timestamp':{'$gte':%d,'$lte':%d}}", from, to);
        return list(Document.parse(query));
    }

    public static void deleteByIsinAndTimestamp(String isin, long timestamp){
        delete("isin = ?1 and timestamp <= ?2",isin,timestamp);
    }

    /**
     * Find all quotes by given isins and range, with from being <strong>inclusive</strong> and <strong>to</strong> being exclusive
     * @param isins a {@link Collection} of isins
     * @param from the timestamp millis, inclusive
     * @param to the timestamp millis, exclusive
     * @return a {@link List} of quote
     */
    public static List<Quote> findByIsinsInRange(Collection<String> isins, long from, long to) {
        String query = String.format("{'isin': {'$in':[%s]},'timestamp':{'$gte':%d,'$lt':%d}}", isins.stream().map(s -> String.format("'%s'",s)).collect(Collectors.joining(",")), from, to);
        return list(Document.parse(query));
    }

    public static Quote from(CandleStickQuote candlestickQuote) {
        return Quote.builder()
                .isin(candlestickQuote.isin())
                .price(candlestickQuote.price())
                .timestamp(Instant.now().toEpochMilli())
                .type(candlestickQuote.type())
                .build();
    }

    @Override
    public String isin() {
        return isin;
    }

    @Override
    public double price() {
        return price;
    }

    @Override
    public long timestamp() {
        return timestamp;
    }
}
