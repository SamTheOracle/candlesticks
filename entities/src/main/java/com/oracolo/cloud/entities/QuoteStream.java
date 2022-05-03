package com.oracolo.cloud.entities;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.oracolo.cloud.events.CandlestickQuote;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;

@MongoEntity(collection = "quote_data")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@Getter
@Setter
public class QuoteStream extends PanacheMongoEntity implements CandlestickQuote {
    private String isin;
    private double price;
    private String type;
    private long timestamp;

    public static List<QuoteStream> findQuotesInRange(long from, long to) {
        return list("timestamp >=?1 and timestamp <= ?2", from, to);
    }

    public static List<QuoteStream> findQuotasByIsinInRange(Set<String> isins, long from, long now) {
        return list("isin in ?1 and timestamp >= ?2 and timestamp <= ?3", isins, from, now);
    }

    public static QuoteStream from(CandlestickQuote candlestickQuote) {
        return QuoteStream.builder()
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
