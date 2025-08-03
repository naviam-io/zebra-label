package io.naviam.maximo.app.label;

import psdi.mbo.*;
import psdi.util.MXException;

import java.rmi.RemoteException;

/**
 * MboValueAdapter that provides a list of valid storeroom locations.
 *
 * @author Jason VenHuizen
 */
@SuppressWarnings("unused")
public class FldLocation extends MAXTableDomain {

    /**
     * Create a new FldLocation instance.
     *
     * @param mbv the MboValue that is being wrapped by the adapter.
     */
    public FldLocation(MboValue mbv) {
        super(mbv);
        String thisAttr = this.getMboValue().getAttributeName();

        setRelationship("LOCATIONS","location=:location and siteid = :siteid and type = 'STOREROOM'");
        setListCriteria("type = 'STOREROOM' and siteid = :siteid");
        this.setErrorMessage("naviam", "invalidStoreroom");
    }

    @Override
    public void setValueFromLookup(MboRemote sourceMbo) throws MXException, RemoteException {
        if(sourceMbo!=null && sourceMbo.isBasedOn("LOCATIONS")){
            getMboValue("SITEID").setValue(sourceMbo.getString("SITEID"));
            getMboValue("ORGID").setValue(sourceMbo.getString("ORGID"), MboConstants.NOACCESSCHECK| MboConstants.NOVALIDATION);
            getMboValue("LOCATION").setValue(sourceMbo.getString("LOCATION"));
        }
                super.setValueFromLookup(sourceMbo);
    }
}
