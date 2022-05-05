package com.oracolo.cloud.server;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import com.oracolo.cloud.entities.sql.CandleStick;
import com.oracolo.cloud.server.dto.CandleStickDto;

@ApplicationScoped
public class CandleStickConverter {
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	public List<CandleStickDto> from(List<CandleStick> candleSticks) {
		return candleSticks.stream().map(candleStick -> CandleStickDto.builder().openTimestamp(
				DATE_TIME_FORMATTER.format(ZonedDateTime.ofInstant(candleStick.getOpenTimestamp(), ZoneId.systemDefault()))).closeTimestamp(
				DATE_TIME_FORMATTER.format(ZonedDateTime.ofInstant(candleStick.getCloseTimestamp(), ZoneId.systemDefault()))).highPrice(
				candleStick.getHighPrice()).lowPrice(candleStick.getLowPrice()).closePrice(candleStick.getClosePrice()).openPrice(
				candleStick.getOpenPrice()).build()).collect(Collectors.toUnmodifiableList());
	}
}
