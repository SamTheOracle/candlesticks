package com.oracolo.cloud.streamhandler.testdata;

import java.util.Objects;

import com.oracolo.cloud.events.CandleStickInstrument;
import com.oracolo.cloud.events.InstrumentEventType;

public class InstrumentEventTest implements CandleStickInstrument {
	private final String isin,description;
	private final InstrumentEventType type;
	private final long timestamp;

	public InstrumentEventTest(String isin, String description, InstrumentEventType type, long timestamp) {
		this.isin = isin;
		this.description = description;
		this.type = type;
		this.timestamp = timestamp;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof InstrumentEventTest))
			return false;

		InstrumentEventTest that = (InstrumentEventTest) o;

		if (!Objects.equals(isin, that.isin))
			return false;
		if (!Objects.equals(description, that.description))
			return false;
		return type == that.type;
	}

	@Override
	public int hashCode() {
		int result = isin != null ? isin.hashCode() : 0;
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (type != null ? type.hashCode() : 0);
		return result;
	}

	@Override
	public String isin() {
		return isin;
	}

	@Override
	public InstrumentEventType type() {
		return type;
	}

	@Override
	public String description() {
		return description;
	}


	@Override
	public String toString() {
		return "InstrumentEventTest{" + "isin='" + isin + '\'' + ", description='" + description + '\'' + ", type=" + type + '}';
	}
}
