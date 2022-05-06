package com.oracolo.cloud.events;

/**
 * All the information an instrument event can provide
 */
public interface CandleStickInstrument {

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

}
