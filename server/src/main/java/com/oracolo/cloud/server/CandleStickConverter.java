package com.oracolo.cloud.server;

import com.oracolo.cloud.entities.CandleStick;
import com.oracolo.cloud.server.dto.CandleStickDto;

import javax.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class CandleStickConverter {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public List<CandleStickDto> from(List<CandleStick> candleSticks) {
        return candleSticks.stream().map(candleStick -> CandleStickDto.builder()
                .openTimestamp(DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(candleStick.getOpenTimestamp()).atZone(ZoneId.systemDefault())))
                .closeTimestamp(DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(candleStick.getCloseTimestamp()).atZone(ZoneId.systemDefault())))
                .highPrice(candleStick.getHighPrice())
                .lowPrice(candleStick.getLowPrice())
                .closePrice(candleStick.getClosePrice())
                .openPrice(candleStick.getOpenPrice())
                .build())
                .sorted()
                .collect(Collectors.toList());
    }
}
