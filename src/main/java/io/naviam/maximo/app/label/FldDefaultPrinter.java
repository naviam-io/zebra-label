package io.naviam.maximo.app.label;

import psdi.mbo.MboSetRemote;
import psdi.mbo.MboValue;
import psdi.mbo.MboValueAdapter;
import psdi.server.MXServer;
import psdi.util.MXApplicationYesNoCancelException;
import psdi.util.MXException;

import java.rmi.RemoteException;

/**
 * MboValueAdapter that validates that there isn't another default printer for the same location and media type.
 * If there is, the user is prompted if they way to uncheck the other printer as the default.
 *
 * @author Jason VenHuizen
 */
@SuppressWarnings("unused")
public class FldDefaultPrinter extends MboValueAdapter {

    /**
     * Create a new FldDefaultPrinter instance.
     *
     * @param mbv the MboValue that is being wrapped by the adapter.
     */
    public FldDefaultPrinter(MboValue mbv) {
        super(mbv);
    }

    @Override
    public void validate() throws MXException, RemoteException {

        MboSetRemote printerSet = getMboValue().getMbo().getMboSet("$printercheck", "STPRINTER", "printer!=:printer and location = :location and siteid = :siteid  and isdefault = :yes");
        if (!printerSet.isEmpty()) {
            if (getMboValue().getMbo().getUserInfo().isInteractive()) {
                int userInput = MXApplicationYesNoCancelException.getUserInput("changedefaultprinter", MXServer.getMXServer(), getMboValue().getMbo().getUserInfo());
                switch (userInput) {
                    case MXApplicationYesNoCancelException.YES:
                        printerSet.getMbo(0).setValue("ISDEFAULT", false);
                        break;
                    case MXApplicationYesNoCancelException.NULL:
                        throw new MXApplicationYesNoCancelException("changedefaultprinter", "naviam", "changeDefaultPrinter",new String[]{getMboValue("LOCATION").getString()});
                    default:
                        getMboValue().setValue(false);
                }
            } else {
                printerSet.getMbo(0).setValue("ISDEFAULT", false);
            }
        }
        super.validate();
    }
}
