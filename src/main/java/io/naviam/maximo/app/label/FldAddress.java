package io.naviam.maximo.app.label;

import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;
import psdi.mbo.MboValue;
import psdi.mbo.MboValueAdapter;
import psdi.util.MXApplicationException;
import psdi.util.MXException;

import java.rmi.RemoteException;

/**
 * MboValueAdapter that uses the Google Guava host and inet address validation to deter valid printer names or IP addresses.
 *
 * @author Jason VenHuizen
 */
@SuppressWarnings("unused")
public class FldAddress extends MboValueAdapter {

    /**
     * Create a new FldAddress instance.
     *
     * @param mbv the MboValue that is being wrapped by the adapter.
     */
    public FldAddress(MboValue mbv) {
        super(mbv);
    }

    @Override
    public void validate() throws MXException, RemoteException {
        if (!getMboValue().isNull()) {
            //noinspection UnstableApiUsage
            if (!InternetDomainName.isValid(getMboValue().getString()) && !InetAddresses.isInetAddress(getMboValue().getString())) {
                throw new MXApplicationException("naviam", "invalidHost", new String[]{getMboValue().getString()});
            }
        }
        super.validate();
    }
}
