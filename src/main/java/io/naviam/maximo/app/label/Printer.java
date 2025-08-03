package io.naviam.maximo.app.label;

import psdi.mbo.Mbo;
import psdi.mbo.MboConstants;
import psdi.mbo.MboSet;
import psdi.util.MXException;

import java.rmi.RemoteException;

/**
 * Mbo for managing the Printer configurations.
 *
 * @author Jason VenHuizen
 */
public class Printer extends Mbo {
    /**
     * Creates a new instance of Printer.
     *
     * @param ms the owning MboSet.
     * @throws RemoteException thrown if a remote networking error occurs.
     */
    public Printer(MboSet ms) throws RemoteException {
        super(ms);
    }

    @Override
    public void init() throws MXException {
        super.init();

        String[] requiredFields = {"PRINTER", "LOCATION", "SITEID", "ORGID", "MEDIA", "ADDRESS", "PORT"};
        setFieldFlag(requiredFields, MboConstants.REQUIRED, true);

        try {
            if(toBeAdded()){
                setValue("SITEID", getUserInfo().getInsertSite(), MboConstants.NOACTION);
                setValue("ORGID", getMboSet("$site","SITE", "siteid=:siteid").getMbo(0).getString("ORGID"), MboConstants.NOACTION|MboConstants.NOVALIDATION);
            }

            if(isNull("SITEID")){
                setFieldFlag("LOCATION", MboConstants.READONLY, true);
            }
        } catch (RemoteException e) {
            // there is nothing we can do about this, but log the error
            LabelLogger.LABEL_LOGGER.error(e);
        }
    }
}
