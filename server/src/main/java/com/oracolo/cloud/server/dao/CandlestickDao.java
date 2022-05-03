package com.oracolo.cloud.server.dao;

import java.time.Instant;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import com.oracolo.cloud.entities.sql.CandleStick;

@ApplicationScoped
public class CandlestickDao extends BaseDao<CandleStick> {

	public Optional<CandleStick> getCandlestickForOpenTimestamp(Instant openTimestamp){
		return Optional.empty();
	}
}
