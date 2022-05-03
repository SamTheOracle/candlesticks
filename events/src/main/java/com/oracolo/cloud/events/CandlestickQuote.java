package com.oracolo.cloud.events;

/**
 * All the information a quote event can provide
 */
public interface CandlestickQuote {
	String DEFAULT_QUOTE_TYPE = "quote";

	/**
	 * @return the isin of the instrument this quote refers to
	 */
	String isin();

	/**
	 *
	 * @return the type of the quote. It defaults to {@link CandlestickQuote#DEFAULT_QUOTE_TYPE}
	 */
	default String type() {
		return DEFAULT_QUOTE_TYPE;
	}

	/**
	 *
	 * @return the price of the quote
	 */
	double price();

	/**
	 *
	 * @return the timestamp
	 */
	long timestamp();

}
