package io.naviam.maximo.app.label;

import psdi.mbo.MboValue;
import psdi.mbo.MboValueAdapter;
import psdi.util.MXException;

import java.rmi.RemoteException;

/**
 * MboValueAdapter that sets the default flag to false if the media type changes.
 *
 * @author Jason VenHuizen
 */
@SuppressWarnings("unused")
public class FldMedia extends MboValueAdapter {

    /**
     * Create a new FldMedia instance.
     *
     * @param mbv the MboValue that is being wrapped by the adapter.
     */
    public FldMedia(MboValue mbv) {
        super(mbv);
    }

    @Override
    public void action() throws MXException, RemoteException {
        getMboValue("ISDEFAULT").setValue(false);
        super.action();
    }
}
