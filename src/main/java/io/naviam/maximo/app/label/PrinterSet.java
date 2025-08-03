package io.naviam.maximo.app.label;

import psdi.mbo.Mbo;
import psdi.mbo.MboServerInterface;
import psdi.mbo.MboSet;

import java.rmi.RemoteException;

/**
 * A MboSet for the Printer configuration table.
 *
 * @author Jason VenHuizen
 */
public class PrinterSet extends MboSet {

    /**
     * Creates a new instance of PrinterSet.
     *
     * @param ms the owning mbo server.
     * @throws RemoteException thrown if a network error occurs.
     */
    public PrinterSet(MboServerInterface ms) throws RemoteException {
        super(ms);
    }

    @Override
    protected Mbo getMboInstance(MboSet mboSet) throws RemoteException {
        return new Printer(mboSet);
    }
}
