package com.oracolo.cloud.server;

import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import com.oracolo.cloud.entities.InstrumentStream;
import com.oracolo.cloud.entities.QuoteStream;
import com.oracolo.cloud.entities.sql.CandleStick;
import com.oracolo.cloud.entities.sql.Instrument;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.runtime.ShutdownEvent;

@ApplicationScoped
public class ApplicationLifecycle {

	void onStop(@Observes ShutdownEvent shutdownEvent){
		QuarkusTransaction.run(()->{
			Instrument.deleteAll();
			CandleStick.deleteAll();
		});
		QuoteStream.deleteAll();
		InstrumentStream.deleteAll();
	}
}
