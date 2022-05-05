package com.oracolo.cloud.entities.sql;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "candlesticks")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@Getter
@Setter
public class CandleStick extends PanacheEntityBase {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

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
		return list("instrument.isin = ?1 and openTimestamp >= ?2 and closeTimestamp <= ?3", isin, from, to);
	}

	public static Optional<CandleStick> findByOpenTimestamp(Instant openTimestamp, Instrument instrument) {
		return find("openTimestamp=?1 and instrument=?2", openTimestamp, instrument).singleResultOptional();
	}
	public static Optional<CandleStick> findByOpenTimestamp(Instant openTimestamp, String isin) {
		return find("openTimestamp=?1 and instrument.isin=?2", openTimestamp, isin).singleResultOptional();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CandleStick))
			return false;

		CandleStick that = (CandleStick) o;

		return id.equals(that.id);
	}

	@Override
	public String toString() {
		return "CandleStick{" + "instrument=" + instrument + ", openTimestamp=" + openTimestamp + ", closeTimestamp=" + closeTimestamp
				+ ", openPrice=" + openPrice + ", closePrice=" + closePrice + ", highPrice=" + highPrice + ", lowPrice=" + lowPrice
				+ ", id=" + id + '}';
	}
}
