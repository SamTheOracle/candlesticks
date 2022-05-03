package com.oracolo.cloud.entities;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.oracolo.cloud.events.CandlestickQuote;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "quote_data")
public class QuoteStream extends PanacheMongoEntity implements CandlestickQuote {
	private String isin;
	private double price;
	private String type;
	private long timestamp;

	public String getIsin() {
		return isin;
	}

	public double getPrice() {
		return price;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getType() {
		return type;
	}

	public void setIsin(String isin) {
		this.isin = isin;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public QuoteStream(String isin, double price, String type, long timestamp) {
		this.isin = isin;
		this.price = price;
		this.type = type;
		this.timestamp = timestamp;
	}

	public QuoteStream() {
	}

	public static List<QuoteStream> findQuotasByIsinInRange(Set<String> isins, long from, long now) {
		return list("isin in ?1 and timestamp >= ?2 and timestamp <= ?3", isins, from, now);
	}

	public static QuoteStream from(CandlestickQuote candlestickQuote) {
		return new QuoteStream(candlestickQuote.isin(), candlestickQuote.price(), candlestickQuote.type(), candlestickQuote.timestamp());
	}

	@Override
	public String isin() {
		return isin;
	}

	@Override
	public double price() {
		return price;
	}

	@Override
	public long timestamp() {
		return timestamp;
	}
}
