package com.oracolo.cloud.streamhandler;

import java.util.List;

import com.oracolo.cloud.events.CandlestickInstrument;
import com.oracolo.cloud.events.CandlestickQuote;

/**
 * It groups together the instrument with its quotes
 */
public interface QuotedInstrument extends CandlestickInstrument {

	/**
	 * The quotes related the instruments
	 * @return a {@link List} of {@link CandlestickQuote}
	 */
	List<CandlestickQuote> quotes();
}
