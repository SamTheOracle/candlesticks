package com.oracolo.cloud.entities.sql;

import java.time.Instant;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Table(name = "candlesticks")
public class CandleStick extends PanacheEntity {

	@ManyToOne
	@JoinColumn(name = "instrument_id")
	private Instrument instrument;

	@Column(name = "open_timestamp")
	private Instant openTimestamp;

	@Column(name = "close_timestamp")
	private Instant closeTimestamp;

	@Column(name = "open_price")
	private Double openPrice;

	@Column(name = "close_price")
	private Double closePrice;

	@Column(name = "high_price")
	private Double highPrice;

	@Column(name = "low_price")
	private Double lowPrice;

	public static void updateFully(CandleStick candleStick) {
		getEntityManager().merge(candleStick);
	}

	public static List<CandleStick> findByIsinAndRange(String isin, Instant from, Instant to) {
		return list("isin = ?1 and openTimestamp >= from and openTimestamp <= to");
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	public Instant getOpenTimestamp() {
		return openTimestamp;
	}

	public void setOpenTimestamp(Instant openTimestamp) {
		this.openTimestamp = openTimestamp;
	}

	public Instant getCloseTimestamp() {
		return closeTimestamp;
	}

	public void setCloseTimestamp(Instant closeTimestamp) {
		this.closeTimestamp = closeTimestamp;
	}

	public Double getOpenPrice() {
		return openPrice;
	}

	public void setOpenPrice(Double openPrice) {
		this.openPrice = openPrice;
	}

	public Double getClosePrice() {
		return closePrice;
	}

	public void setClosePrice(Double closePrice) {
		this.closePrice = closePrice;
	}

	public Double getHighPrice() {
		return highPrice;
	}

	public void setHighPrice(Double highPrice) {
		this.highPrice = highPrice;
	}

	public Double getLowPrice() {
		return lowPrice;
	}

	public void setLowPrice(Double lowPrice) {
		this.lowPrice = lowPrice;
	}

}
