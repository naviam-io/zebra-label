package io.naviam.maximo.app.label;

import psdi.mbo.Mbo;
import psdi.mbo.MboServerInterface;
import psdi.mbo.MboSet;

import java.rmi.RemoteException;

/**
 * A MboSet for registering the print agent.
 *
 * @author Jason VenHuizen
 */
public class PrintAgentSet extends MboSet {

    /**
     * Creates a new instance of PrintAgentSet.
     *
     * @param ms the owning mbo server.
     * @throws RemoteException thrown if a network error occurs.
     */
    public PrintAgentSet(MboServerInterface ms) throws RemoteException {
        super(ms);
    }

    @Override
    protected Mbo getMboInstance(MboSet mboSet) throws RemoteException {
        return new PrintAgent(mboSet);
    }
}
