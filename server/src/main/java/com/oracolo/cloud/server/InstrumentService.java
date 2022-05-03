package com.oracolo.cloud.server;

import java.time.Instant;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.oracolo.cloud.entities.sql.Instrument;
import com.oracolo.cloud.events.CandlestickInstrument;
import com.oracolo.cloud.events.InstrumentEventType;


@ApplicationScoped
public class InstrumentService {

	public void onNewInstrument(@Observes CandlestickInstrument candlestickInstrument){
		if(candlestickInstrument.type()== InstrumentEventType.DELETE){
			Instrument.deleteById(candlestickInstrument.isin());
			return;
		}
		Instrument instrument = new Instrument();
		instrument.setIsin(candlestickInstrument.isin());
		instrument.setDescription(candlestickInstrument.isin());
		instrument.setTimestamp(Instant.now());
		instrument.persist();
	}
}
