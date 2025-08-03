package io.naviam.maximo.app.label;

import psdi.mbo.MboValue;
import psdi.mbo.MboValueAdapter;
import psdi.util.MXApplicationException;
import psdi.util.MXException;

import java.rmi.RemoteException;

/**
 * MboValueAdapter that validates that a minimum the ZPL starts and ends with the required ^XA and ^XZ commands.
 *
 * @author Jason VenHuizen
 */
@SuppressWarnings("unused")
public class FldZPL extends MboValueAdapter {

    /**
     * Create a new FldZPL instance.
     *
     * @param mbv the MboValue that is being wrapped by the adapter.
     */
    public FldZPL(MboValue mbv) {
        super(mbv);
    }

    @Override
    public void validate() throws MXException, RemoteException {
        if (!getMboValue().isNull()) {
            String value = getMboValue().getString();

            if(!value.trim().startsWith("^XA") && !value.trim().endsWith("^XZ")){
                throw new MXApplicationException("naviam", "invalidZPL");
            }
        }
        super.validate();
    }
}
