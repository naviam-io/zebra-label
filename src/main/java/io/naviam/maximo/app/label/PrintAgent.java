package io.naviam.maximo.app.label;

import psdi.mbo.Mbo;
import psdi.mbo.MboConstants;
import psdi.mbo.MboSet;
import psdi.util.MXException;

import java.rmi.RemoteException;

/**
 * Mbo for managing the Print Agent registration.
 *
 * @author Jason VenHuizen
 */
public class PrintAgent extends Mbo {
    /**
     * Creates a new instance of PrintAgent.
     *
     * @param ms the owning MboSet.
     * @throws RemoteException thrown if a remote networking error occurs.
     */
    public PrintAgent(MboSet ms) throws RemoteException {
        super(ms);
    }
}
