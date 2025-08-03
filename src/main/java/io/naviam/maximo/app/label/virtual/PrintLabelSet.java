package io.naviam.maximo.app.label.virtual;

import com.ibm.tivoli.maximo.script.ScriptAction;
import io.naviam.maximo.app.label.LabelLogger;
import psdi.common.context.UIContext;
import psdi.mbo.*;
import psdi.server.MXServer;
import psdi.util.MXApplicationException;
import psdi.util.MXException;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

/**
 * Non-persistent MboSet for managing printing a label.
 *
 * @author Jason VenHuizen
 */
@SuppressWarnings("unused")
public class PrintLabelSet extends NonPersistentMboSet {

    /**
     * Creates a new instance of PrintLabelSet.
     *
     * @param ms the owning mbo server.
     * @throws RemoteException thrown if a network error occurs.
     */
    public PrintLabelSet(MboServerInterface ms) throws RemoteException {
        super(ms);
    }

    @Override
    protected Mbo getMboInstance(MboSet mboSet) throws MXException, RemoteException {
        return new PrintLabel(mboSet);
    }

    @Override
    public void execute() throws MXException, RemoteException {
        MboRemote printLabel = getMbo(0);

        if (printLabel.getInt("COUNT") < 1) {
            throw new MXApplicationException("naviam", "countLessThanOne");
        }

        int maxCount = 10;

        try {
            String max = MXServer.getMXServer().getProperty("naviam.zebralabel.maxcount");
            if (max != null && !max.isEmpty()) {
                maxCount = Integer.parseInt(MXServer.getMXServer().getProperty("naviam.zebralabel.maxcount"));
            }
        } catch (Throwable t) {
            LabelLogger.LABEL_LOGGER.error("Error parsing property naviam.zebralabel.maxcount: " + t.getMessage());
        }

        if (printLabel.getInt("COUNT") > maxCount) {
            throw new MXApplicationException("naviam", "countGreaterThanMax", new String[]{printLabel.getString("COUNT"), String.valueOf(maxCount)});
        }

        for (int i = 0; i < printLabel.getInt("COUNT"); i++) {
            // invoking the script should only throw MXExceptions which can be handled by the application framework.
            try {
                (new ScriptAction()).applyCustomAction(printLabel, new String[]{"STAUTOSCRIPT.ZEBRALABEL.PRINTLABEL"});
            } catch (Throwable e) {

                if (e instanceof MXException) {
                    throw e;
                } else {
                    throw new MXApplicationException("naviam", "unknownPrintError", new String[]{e.getMessage()});
                }
            }
        }

        UIContext uiContext = UIContext.getCurrentContext();
        if (uiContext != null) {

            String[] args ={"unknown"};

            if (getOwner().isBasedOn("ASSET")) {
                args = new String[]{"asset",getOwner().getString( "ASSETNUM")};
            } else if (getOwner().isBasedOn("INVBALANCES")|| getOwner().isBasedOn("INVENTORY")){
                args = new String[]{"item",getOwner().getString("ITEMNUM")};
            }else if (getOwner().isBasedOn("MATRECTRANS")){
                args = new String[]{"poline", getOwner().getString("POLINENUM")};
            }else if (getOwner().isBasedOn("LOCATIONS")){
                args = new String[]{"location", getOwner().getString("LOCATION")};
            } else if (getOwner().isBasedOn("WORKORDER")){
                args = new String[]{"workorder", getOwner().getString("WONUM")};
            }
            Object wcs = uiContext.getWebClientSession();
            try {
                Method m = wcs.getClass().getMethod("showMessageBox", String.class, String.class, Array.newInstance(String.class, 0).getClass());
                m.invoke(wcs, "naviam", "labelPrinted", args);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
                // intentionally ignored.
            }

        }


        // reset the MboSet so related sets such as the temporary domain used for the combo boxes isn't saved.
        reset();
    }
}
