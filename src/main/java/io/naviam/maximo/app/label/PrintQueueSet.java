package io.naviam.maximo.app.label;

import psdi.mbo.Mbo;
import psdi.mbo.MboServerInterface;
import psdi.mbo.MboSet;

import java.rmi.RemoteException;

/**
 * A MboSet for managing the print queue.
 *
 * @author Jason VenHuizen
 */
public class PrintQueueSet extends MboSet {

    /**
     * Creates a new instance of PrintQueueSet.
     *
     * @param ms the owning mbo server.
     * @throws RemoteException thrown if a network error occurs.
     */
    public PrintQueueSet(MboServerInterface ms) throws RemoteException {
        super(ms);
    }

    @Override
    protected Mbo getMboInstance(MboSet mboSet) throws RemoteException {
        return new PrintQueue(mboSet);
    }
}
