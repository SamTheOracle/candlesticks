package com.oracolo.cloud.events;

import java.time.Instant;

public class InstrumentEvent implements CandlestickInstrument {

	public InstrumentEventType type;
	public InstrumentData data;


	@Override
	public String toString() {
		return "InstrumentEvent{" + "type=" + type + ", data=" + data + '}';
	}

	@Override
	public String isin() {
		return data.isin;
	}

	@Override
	public InstrumentEventType type() {
		return type;
	}

	@Override
	public String description() {
		return data.description;
	}

}
