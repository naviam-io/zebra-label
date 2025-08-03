package io.naviam.maximo.app.label;

import psdi.mbo.MboValue;
import psdi.mbo.MboValueAdapter;
import psdi.util.MXApplicationException;
import psdi.util.MXException;

import java.rmi.RemoteException;

/**
 * MboValueAdapter that validates that the entered value is between 0 and 65535.
 *
 * @author Jason VenHuizen
 */
@SuppressWarnings("unused")
public class FldPort extends MboValueAdapter {

    /**
     * Create a new FldPort instance.
     *
     * @param mbv the MboValue that is being wrapped by the adapter.
     */
    public FldPort(MboValue mbv) {
        super(mbv);
    }

    @Override
    public void validate() throws MXException, RemoteException {
        if (!getMboValue().isNull()) {
            int port = getMboValue().getInt();

            if(port <0 || port > 65535){
                throw new MXApplicationException("naviam","invalidPort",new String[]{String.valueOf(port)});
            }
        }
        super.validate();
    }
}
