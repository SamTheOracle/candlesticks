package com.oracolo.cloud.entities;


import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.Document;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * It represents a <a href="https://en.wikipedia.org/wiki/Candlestick_chart">candlestick</a>, with the following properties:
 * <ul>
 *     <li>
 *         <strong>isin</strong> is the instrument isin
 *     </li>
 *     <li> <strong>openTimestamp</strong> the minute this candlestick refers to</li>
 *     <li>
 *         <strong>closeTimestamp</strong> the closing minute
 *     </li>
 *     <li>
 *         <strong>closePrice</strong> is the last {@link Quote}'s price received in the minute
 *     </li>
 *     <li>
 *         <strong>openPrice</strong> it is the first {@link Quote}'s price received in the minute
 *     </li>
 *     <li>
 *         <strong>highPrice</strong> it is the {@link Quote} with the highest price in the minute
 *     </li>
 *     <li>
 *         <strong>lowPrice</strong> it is the {@link Quote} with the lowest price in the minute
 *     </li>
 * </ul>
 */
@MongoEntity(collection = "candlestick_data", database = "streamevents")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@Getter
@Setter
public class CandleStick extends PanacheMongoEntity {

    private String isin;
    private long openTimestamp, closeTimestamp;
    private double openPrice, closePrice, highPrice, lowPrice;

    /**
     * Finds {@link CandleStick} for an instrument, in the given range
     *
     * @param isin the {@link Instrument} isin
     * @param from the temporal lower bound limit, inclusive
     * @param to   the temporal higher bound limit, inclusive
     * @return a {@link List} of {@link CandleStick}
     */
    public static List<CandleStick> findByIsinAndRange(String isin, long from, long to) {
        String query = String.format("{'isin':'%s', '$and':[{'openTimestamp':{'$gte':%d}}, {'closeTimestamp':{'$lte':%d}}]}", isin, from, to);
        return list(Document.parse(query));
    }

    public static List<CandleStick> findByIsinAndRange(String isin, Instant from, Instant to) {
        return findByIsinAndRange(isin, from.toEpochMilli(), to.toEpochMilli());
    }

    public static Optional<CandleStick> findByOpenTimestampAndIsin(long open, String isin) {
        return find("isin = ?1 and openTimestamp = ?2", isin, open).firstResultOptional();
    }

    public static Optional<CandleStick> findByOpenTimestampAndIsin(Instant open, String isin) {
        return findByOpenTimestampAndIsin(open.toEpochMilli(), isin);
    }

    public static void deleteByIsinClose(String isin, long close) {
        delete("isin = ?1 and closeTimestamp <= ?2", isin, close);
    }

    /**
     * Empty candlestick is a candlestick that convers the minute and has an isin, but with no prices
     *
     * @param isin           the {@link Instrument} isin
     * @param openTimestamp  the openTimestamp
     * @param closeTimestamp the closeTimestamp
     * @return an instance of {@link CandleStick}
     */
    public static CandleStick empty(String isin, Instant openTimestamp, Instant closeTimestamp) {
        return CandleStick.builder().isin(isin)
                .closeTimestamp(closeTimestamp.toEpochMilli())
                .openTimestamp(openTimestamp.toEpochMilli())
                .closePrice(0.0)
                .highPrice(0.0)
                .lowPrice(0.0)
                .openPrice(0.0)
                .build();
    }
}
