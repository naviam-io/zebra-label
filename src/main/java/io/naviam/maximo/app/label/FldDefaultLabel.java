package io.naviam.maximo.app.label;

import psdi.mbo.MboConstants;
import psdi.mbo.MboSetRemote;
import psdi.mbo.MboValue;
import psdi.mbo.MboValueAdapter;
import psdi.server.MXServer;
import psdi.util.MXApplicationYesNoCancelException;
import psdi.util.MXException;

import java.rmi.RemoteException;

/**
 * MboValueAdapter that validates that there isn't another default label for the same  media type.
 * If there is, the user is prompted if they way to uncheck the other label as the default.
 *
 * @author Jason VenHuizen
 */
@SuppressWarnings("unused")
public class FldDefaultLabel extends MboValueAdapter {

    /**
     * Create a new FldDefaultLabel instance.
     *
     * @param mbv the MboValue that is being wrapped by the adapter.
     */
    public FldDefaultLabel(MboValue mbv) {
        super(mbv);
    }

    @Override
    public void validate() throws MXException, RemoteException {
        if(getMboValue().getBoolean()) {
            MboSetRemote LabelSet = getMboValue().getMbo().getMboSet("$labelcheck", "STLABEL", "media = :media and isdefault = :yes and usewith =:usewith and label!=:label");
            if (!LabelSet.isEmpty()) {
                if (getMboValue().getMbo().getUserInfo().isInteractive()) {
                    int userInput = MXApplicationYesNoCancelException.getUserInput("changedefaultlabel", MXServer.getMXServer(), getMboValue().getMbo().getUserInfo());
                    switch (userInput) {
                        case MXApplicationYesNoCancelException.YES:
                            LabelSet.getMbo(0).setValue("ISDEFAULT", false, MboConstants.NOVALIDATION);
                            break;
                        case MXApplicationYesNoCancelException.NULL:
                            throw new MXApplicationYesNoCancelException("changedefaultlabel", "naviam", "changeDefaultLabel", new String[]{getMboValue("MEDIA").getString()});
                        default:
                            getMboValue().setValue(false);
                    }
                } else {
                    LabelSet.getMbo(0).setValue("ISDEFAULT", false);
                }
            }
        }
        super.validate();
    }
}
