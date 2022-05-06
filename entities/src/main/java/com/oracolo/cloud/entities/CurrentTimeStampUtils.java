package com.oracolo.cloud.entities;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class CurrentTimeStampUtils {

	public static Instant currentMinuteTimestamp() {
		ZonedDateTime instant = ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		return Instant.from(instant.withSecond(0).withNano(0));
	}
	public static Instant instantRoundedAtMinute(Instant instant){
		return Instant.from(ZonedDateTime.ofInstant(instant,ZoneOffset.UTC).withSecond(0).withNano(0));
	}
}
