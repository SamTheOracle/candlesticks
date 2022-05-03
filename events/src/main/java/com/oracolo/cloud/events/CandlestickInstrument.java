package com.oracolo.cloud.events;

import java.time.Instant;

/**
 * All the information an instrument event can provide
 */
public interface CandlestickInstrument {

	/**
	 * @return the identifier of the instrument as {@link String}
	 */
	String isin();

	/**
	 * @return the {@link InstrumentEventType}
	 */
	InstrumentEventType type();

	/**
	 *
	 * @return the instrument description as {@link String}
	 */
	String description();

	/**
	 *
	 * @return creation timestamp. It defaults to current timestamp in milliseconds
	 */
	default long timestamp(){
		return Instant.now().toEpochMilli();
	}
}
