package com.nachtraben.core.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Created by NachtRaben on 3/6/2017.
 */
public class LogbackListener<E> extends AppenderBase<E> {

    @Override
    protected void append(E eventObject) {
        if(eventObject instanceof ILoggingEvent) {
            ILoggingEvent event = (ILoggingEvent) eventObject;
            System.out.println(event.getFormattedMessage());
        }
    }

}
