package io.naviam.maximo.app.label;

import psdi.server.AppService;
import psdi.server.MXServer;

import java.rmi.RemoteException;

/**
 * A service for the Zebra Label Printing.
 *
 * @author Jason VenHuizen
 */
public class LabelService extends AppService {

    /**
     * Creates a new instance of the LabelService class.
     *
     * @throws RemoteException thrown if a remote networking error occurs.
     */
    @SuppressWarnings("unused")
    public LabelService() throws RemoteException {
        super();
        registerCache();
    }

    /**
     * Creates a new instance of the LabelService class.
     *
     * @param mxServer the owning Maximo application server.
     * @throws RemoteException thrown if a remote networking error occurs.
     */
    @SuppressWarnings("unused")
    public LabelService(MXServer mxServer) throws RemoteException {
        super(mxServer);
        registerCache();
    }

    /**
     * Register the zebra label printing cache.
     */
    private void registerCache(){
        if(LabelLogger.LABEL_LOGGER.isDebugEnabled()) {
            LabelLogger.LABEL_LOGGER.debug("Creating and registering the zebra label printing cache.");
        }

        ZebraLabelPrintCache cache = ZebraLabelPrintCache.getCache();
        getMXServer().addToMaximoCache(cache.getName(), cache);
    }
}