package io.naviam.maximo.app.label.virtual;

import io.naviam.maximo.app.label.LabelLogger;
import psdi.mbo.*;
import psdi.util.MXException;

import java.rmi.RemoteException;

/**
 * MboValueAdapter that dynamically generates a value list for the Label combo box on the Print Label dialog.
 *
 * @author Jason VenHuizen
 */
public class FldLabel extends MboValueAdapter {

    /**
     * Create a new FldLabel instance.
     *
     * @param mbv the MboValue that is being wrapped by the adapter.
     */
    public FldLabel(MboValue mbv) {
        super(mbv);
    }

    /**
     * {@inerhitDoc}
     *
     * @see MboValueAdapter#getList()
     */
    @Override
    public MboSetRemote getList() throws MXException, RemoteException {
        MboSetRemote domainSet = getMboValue().getMbo().getMboSet("$tmpmaxdomain", "MAXDOMAIN", "1=0");
        domainSet.reset();
        MboRemote domain = domainSet.add(NOACCESSCHECK);
        domain.setValue("DOMAINID", "STLABELTMP");
        domain.setValue("DOMAINTYPE", "ALN");
        domain.setValue("MAXTYPE", getMboValue().getMboValueInfo().getMaxType());
        domain.setValue("LENGTH", getMboValue().getMboValueInfo().getLength());

        MboSetRemote alnSet = domain.getMboSet("ALNDOMAINVALUE");

        MboSetRemote printerSet = getMboValue().getMbo().getMboSet("$stprinter", "STPRINTER", "printer = :printer");

        if (!printerSet.isEmpty()) {
            String media = printerSet.moveFirst().getString("MEDIA");

            SqlFormat sqlf = new SqlFormat("media = :1 and usewith = :2");
            sqlf.setObject(1, "STLABEL", "MEDIA", media);
            if (getMboValue().getMbo().getOwner() != null) {
                sqlf.setObject(2, "STLABEL", "USEWITH", getMboValue().getMbo().getOwner().getName());
            } else {
                LabelLogger.LABEL_LOGGER.error("The value adapter " + getClass().getName() + " on " + getMboValue().getMbo().getName() + " must have an owner to display a list of available labels.");
                sqlf.setObject(2, "STLABEL", "USEWITH", "DUMMY");
            }

            MboSetRemote labelSet = getMboValue().getMbo().getMboSet("$stlabel", "STLABEL", sqlf.format());
            labelSet.clear();
            labelSet.reset();
            MboRemote label = labelSet.moveFirst();
            while (label != null) {
                MboRemote aln = alnSet.add(NOACCESSCHECK | NOACTION | NOVALIDATION);
                aln.setValue("VALUE", label.getString("LABEL"));
                aln.setValue("DESCRIPTION", label.getString("DESCRIPTION"));
                label = labelSet.moveNext();
            }

            return alnSet;
        } else {
            return super.getList();
        }
    }

    @Override
    public boolean hasList() {
        return true;
    }
}
