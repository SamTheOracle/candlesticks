package com.oracolo.cloud.streamhandler;

import java.util.List;

import com.oracolo.cloud.events.CandleStickInstrument;
import com.oracolo.cloud.events.CandleStickQuote;

/**
 * It groups together the instrument with its quotes
 */
public interface QuotedInstrument extends CandleStickInstrument {

	/**
	 * The quotes related the instruments
	 * @return a {@link List} of {@link CandleStickQuote}
	 */
	List<CandleStickQuote> quotes();
}
