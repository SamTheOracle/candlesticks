package com.oracolo.cloud.server;

import com.oracolo.cloud.entities.CandleStick;
import com.oracolo.cloud.entities.Instrument;
import com.oracolo.cloud.entities.Quote;
import com.oracolo.cloud.server.ws.QuoteClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.websocket.CloseReason;

@ApplicationScoped
public class WSCloseEventListener {


    public void onClose(@Observes CloseReason closeReason){
        CandleStick.deleteAll();
        Instrument.deleteAll();
        Quote.deleteAll();
    }

}
