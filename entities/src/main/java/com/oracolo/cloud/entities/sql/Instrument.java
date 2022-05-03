package com.oracolo.cloud.entities.sql;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "instruments")
public class Instrument extends PanacheEntityBase {

	@Id
	@Column(name = "isin")
	private String isin;

	@Column(name = "timestamp")
	private Instant timestamp;

	@Column(name = "description")
	private String description;


	public String getIsin() {
		return isin;
	}

	public void setIsin(String isin) {
		this.isin = isin;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Instrument))
			return false;

		Instrument that = (Instrument) o;

		return isin.equals(that.isin);
	}

	@Override
	public int hashCode() {
		return isin.hashCode();
	}


}
