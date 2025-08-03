package io.naviam.maximo.app.label;

import psdi.server.event.EventListener;
import psdi.server.event.EventMessage;
import psdi.util.MXException;

public class PrintLabelEventListener implements EventListener {
    @Override
    public boolean eventValidate(EventMessage eventMessage) throws MXException {
        return false;
    }

    @Override
    public void preSaveEventAction(EventMessage eventMessage) throws MXException {

    }

    @Override
    public void eventAction(EventMessage eventMessage) throws MXException {

    }

    @Override
    public void postCommitEventAction(EventMessage eventMessage) throws MXException {

    }
}
