package io.naviam.maximo.webclient.beans.zebralabel;

import psdi.iface.mic.MicUtil;
import psdi.mbo.Mbo;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.util.MXException;
import psdi.webclient.controls.Dialog;
import psdi.webclient.system.beans.DataBean;

import java.rmi.RemoteException;

/**
 * Bean that handles only displaying the print label dialog if more than one printer / label combination is available.
 *
 * @author Jason VenHuizen
 */
@SuppressWarnings("unused")
public class PrintLabelBean extends DataBean {
    @Override
    protected void initialize() throws MXException, RemoteException {
        super.initialize();



        if (!getMboSet().isEmpty()) {
            Mbo printLabel = (Mbo) getMboSet().getMbo(0);

            if (printLabel.getOwner() != null) {

                boolean allowReceiptSiteOnly = false;
                try{

                    allowReceiptSiteOnly = MicUtil.getBooleanProperty("naviam.zebralabel.receiptSiteOnly", false);
                }catch(Exception e){
                    // Ignore errors and use the default value
                }
                boolean isReceiptWithoutLocation = printLabel.getOwner().getName().equals("MATRECTRANS") && printLabel.getOwner().isNull("TOSTORELOC");
                if(!allowReceiptSiteOnly && isReceiptWithoutLocation) {
                    if (printLabel.getOwner().getName().equals("MATRECTRANS") && printLabel.getOwner().isNull("TOSTORELOC")) {
                        clientSession.showMessageBox("naviam", "noToStoreLoc", null);
                        ((Dialog) clientSession.findDialog("zebralabelprint")).dialogcancel();
                    }
                }

                MboSetRemote printerList;
                if(isReceiptWithoutLocation){
                    printerList =printLabel.getMboSet("$stprinter", "STPRINTER", "siteid = :siteid");
                }else {
                    printerList =printLabel.getMboSet("$stprinter", "STPRINTER", "location=:location and siteid = :siteid");
                }
                if (printerList!=null && printerList.count() == 1) {
                    MboRemote printer = printerList.getMbo(0);
                    setValue("PRINTER", printer.getString("PRINTER"));
                    MboSetRemote labelList = printer.getMboSet("$stlabel", "STLABEL", "media=:media and usewith = '" + printLabel.getOwner().getName() + "'");

                    if(labelList.count() == 1){
                        MboRemote label = labelList.getMbo(0);

                        ((Dialog) clientSession.findDialog("zebralabelprint")).dialogok();

                        printLabel.setValue("PRINTER", printer.getString("PRINTER"));
                        printLabel.setValue("LABEL", label.getString("LABEL"));

                    }
                }
            }
        }
    }
}

