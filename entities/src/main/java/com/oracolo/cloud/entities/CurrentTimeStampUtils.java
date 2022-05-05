package com.oracolo.cloud.entities;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class CurrentTimeStampUtils {

	public static Instant currentMinuteTimestamp() {
		ZonedDateTime instant = ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		return Instant.from(instant.withSecond(0).withNano(0));
	}
}
