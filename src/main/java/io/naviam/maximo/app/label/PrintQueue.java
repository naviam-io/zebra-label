package io.naviam.maximo.app.label;

import psdi.mbo.Mbo;
import psdi.mbo.MboSet;

import java.rmi.RemoteException;

/**
 * Mbo for managing the remote Print Queue.
 *
 * @author Jason VenHuizen
 */
public class PrintQueue extends Mbo {
    /**
     * Creates a new instance of PrintQueue.
     *
     * @param ms the owning MboSet.
     * @throws RemoteException thrown if a remote networking error occurs.
     */
    public PrintQueue(MboSet ms) throws RemoteException {
        super(ms);
    }
}
