package com.oracolo.cloud.events;

public class InstrumentData {
	public String description;
	public String isin;

	@Override
	public String toString() {
		return "InstrumentData{" + "description='" + description + '\'' + ", isin='" + isin + '\'' + '}';
	}
}
