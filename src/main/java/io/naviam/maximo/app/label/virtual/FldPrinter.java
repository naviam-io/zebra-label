package io.naviam.maximo.app.label.virtual;

import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.mbo.MboValue;
import psdi.mbo.MboValueAdapter;
import psdi.util.MXException;

import java.rmi.RemoteException;

/**
 * MboValueAdapter that dynamically generates a value list for the Printer combo box on the Print Label dialog.
 *
 * @author Jason VenHuizen
 */
public class FldPrinter extends MboValueAdapter {

    /**
     * Create a new FldPrinter instance.
     *
     * @param mbv the MboValue that is being wrapped by the adapter.
     */
    public FldPrinter(MboValue mbv) {
        super(mbv);
    }

    @Override
    public MboSetRemote getList() throws MXException, RemoteException {

        MboSetRemote domainSet = getMboValue().getMbo().getMboSet("$tmpmaxdomain", "MAXDOMAIN", "1=0");

        domainSet.reset();

        MboRemote domain = domainSet.add(NOACCESSCHECK);
        domain.setValue("DOMAINID", "STPRINTERTMP");
        domain.setValue("DOMAINTYPE", "ALN");
        domain.setValue("MAXTYPE", getMboValue().getMboValueInfo().getMaxType());
        domain.setValue("LENGTH", getMboValue().getMboValueInfo().getLength());

        MboSetRemote alnSet = domain.getMboSet("ALNDOMAINVALUE");

        MboSetRemote printerSet;
        MboRemote owner = getMboValue().getMbo().getOwner();
        if (owner != null && (owner.isBasedOn("ASSET") || owner.isBasedOn("LOCATIONS") || owner.isBasedOn("WORKORDER"))) {
            printerSet = getMboValue().getMbo().getMboSet("$stprinter", "STPRINTER", "siteid = :siteid");
        } else {
            printerSet = getMboValue().getMbo().getMboSet("$stprinter", "STPRINTER", "location = :location and siteid = :siteid");
        }
        printerSet.clear();
        printerSet.reset();

        MboRemote printer = printerSet.moveFirst();
        while (printer != null) {
            MboRemote aln = alnSet.add(NOACCESSCHECK | NOACTION | NOVALIDATION);
            aln.setValue("VALUE", printer.getString("PRINTER"));
            aln.setValue("DESCRIPTION", printer.getString("DESCRIPTION"));
            printer = printerSet.moveNext();
        }

        return alnSet;
    }

    @Override
    public void action() throws MXException, RemoteException {
        MboSetRemote labelList = getMboValue("LABEL").getList();
        if (labelList != null && labelList.count() == 1) {
            getMboValue("LABEL").setValue(labelList.getMbo(0).getString("VALUE"));
        } else {
            if(labelList!=null) {
                boolean foundDefault = false;
                MboRemote label = labelList.moveFirst();
                while (label != null) {
                    MboSetRemote labelSet = label.getMboSet("$stlabel", "STLABEL", "label=:value");

                    if (!labelSet.isEmpty() && labelSet.getMbo(0).getBoolean("ISDEFAULT")) {
                        foundDefault = true;
                        getMboValue("LABEL").setValue(label.getString("VALUE"));
                        break;
                    }
                    label = labelList.moveNext();
                }

                if (!foundDefault) {
                    getMboValue("LABEL").setValueNull();
                }
            }
        }
    }

    @Override
    public boolean hasList() {
        return true;
    }
}
