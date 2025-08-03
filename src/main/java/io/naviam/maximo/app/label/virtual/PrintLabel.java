package io.naviam.maximo.app.label.virtual;

import psdi.mbo.MboRemote;
import psdi.mbo.MboSet;
import psdi.mbo.MboSetRemote;
import psdi.mbo.NonPersistentMbo;
import psdi.util.MXException;

import java.rmi.RemoteException;

/**
 * Mbo for managing printing a label.
 *
 * @author Jason VenHuizen
 */
public class PrintLabel extends NonPersistentMbo {

    /**
     * Create a new instance of the PrintLabel Mbo.
     * @param ms the owning MboSet.
     * @throws RemoteException thrown if a remote network error occurs.
     */
    public PrintLabel(MboSet ms) throws RemoteException {
        super(ms);
    }

    @Override
    public void init() throws MXException {
        super.init();
        setFieldFlag(new String[]{"PRINTER", "LABEL", "COUNT"}, REQUIRED, true);
    }

    @Override
    public void add() throws MXException, RemoteException {
        setValue("COUNT", 1);

        if(getOwner()!=null){
            MboRemote owner = getOwner();

            setValue("PARENT", owner.getName());
            setValue("PARENTID", owner.getUniqueIDValue());

            if (owner.getMboValueInfoStatic("TOSTORELOC")!=null){
                setValue("LOCATION", owner.getString("TOSTORELOC"));
            }else if(owner.getMboValueInfoStatic("LOCATION")!=null){
                setValue("LOCATION", owner.getString("LOCATION"));
            }

            if(owner.getMboValueInfoStatic("SITEID") !=null){
                setValue("SITEID", owner.getString("SITEID"));
            }

            MboSetRemote printerSet;
            if(getOwner()!=null && (getOwner().isBasedOn("ASSET") || getOwner().isBasedOn("LOCATIONS") || getOwner().isBasedOn("WORKORDER"))) {
                printerSet = getMboSet("$stprinter", "STPRINTER", "siteid = :siteid and isdefault = :yes");
            }else {
                printerSet = getMboSet("$stprinter", "STPRINTER", "location = :location and siteid = :siteid and isdefault = :yes ");
            }
            printerSet.clear();
            printerSet.reset();

            if(!printerSet.isEmpty()){
                MboRemote printer = printerSet.moveFirst();
                setValue("PRINTER", printer.getString("PRINTER"));

                MboSetRemote labelSet = printer.getMboSet("$stlabel","STLABEL", "media = :media and isdefault = :yes and usewith = :&OWNERNAME&");
                if(!labelSet.isEmpty()){
                    setValue("LABEL", labelSet.moveFirst().getString("LABEL"));
                }
            }
        }
        super.add();
    }
}
