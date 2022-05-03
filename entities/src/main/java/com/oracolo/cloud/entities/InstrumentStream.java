package com.oracolo.cloud.entities;


import java.time.Instant;
import java.util.List;

import com.oracolo.cloud.events.CandlestickInstrument;
import com.oracolo.cloud.events.InstrumentEventType;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "instrument_data")
public class InstrumentStream extends PanacheMongoEntity {

	private String description;
	private long timestamp;
	private String isin;
	private Status status;

	public InstrumentStream(long timestamp, String isin, Status status, String description) {
		this.timestamp = timestamp;
		this.isin = isin;
		this.status = status;
		this.description = description;
	}

	public InstrumentStream() {
	}

	public String getDescription() {
		return description;
	}

	public InstrumentStream setDescription(String description) {
		this.description = description;
		return this;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public InstrumentStream setTimestamp(long timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	public String getIsin() {
		return isin;
	}

	public InstrumentStream setIsin(String isin) {
		this.isin = isin;
		return this;
	}
	public Status getStatus() {
		return status;
	}

	public InstrumentStream setStatus(Status status) {
		this.status = status;
		return this;
	}


	public static List<InstrumentStream> findInstrumentsByRange(long from, long to){
		return list("timestamp >= ?1 and timestamp <= ?2",from,to);
	}



	public static InstrumentStream from(CandlestickInstrument instrumentEvent){
		String isin = instrumentEvent.isin();
		Status status;
		InstrumentEventType instrumentEventType = instrumentEvent.type();
		if(instrumentEventType==InstrumentEventType.ADD){
			status = Status.ADDED;
		}else{
			status = Status.DELETED;
		}
		String description = instrumentEvent.description();
		return new InstrumentStream(Instant.now().toEpochMilli(),isin,status,description);
	}
}
