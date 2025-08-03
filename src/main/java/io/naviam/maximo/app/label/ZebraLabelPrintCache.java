package io.naviam.maximo.app.label;

import com.google.gson.Gson;
import com.ibm.tivoli.maximo.script.ScriptCache;
import com.ibm.tivoli.maximo.script.ScriptDriverFactory;
import com.ibm.tivoli.maximo.script.ScriptInfo;
import com.ibm.tivoli.maximo.script.ScriptService;
import psdi.mbo.*;
import psdi.server.MXServer;
import psdi.server.event.EventMessage;
import psdi.util.MXApplicationException;
import psdi.util.MXException;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Maximo cache that distributes print events to all members to ensure that the print dispatch
 * servlet gets the print event and notifies the attached agent regardless of the instance it is connected to.
 *
 * @author Jason VenHuizen
 */
public class ZebraLabelPrintCache implements MaximoCache {
    private static final String TOPIC = "naviam.printlabel";
    private static final String PRINTERS_SCRIPT = "STAUTOSCRIPT.ZEBRALABEL.PRINTERS";
    private static final String LABELS_SCRIPT = "STAUTOSCRIPT.ZEBRALABEL.LABELS";
    public static final String CACHE_NAME = "ZEBRALABEL";

    private static ZebraLabelPrintCache cache = null;

    /**
     * Gets the singleton instance of the ZebraLabelPrintCache.
     *
     * @return the singleton instance of the ZebraLabelPrintCache.
     */
    public static ZebraLabelPrintCache getCache() {
        if (cache == null) {
            cache = new ZebraLabelPrintCache();
        }

        return cache;
    }

    /**
     * Private constructor to ensure that the ZebraLabelPrintCache is only obtained through the getCache() method.
     */
    private ZebraLabelPrintCache() {
    }

    /**
     * Does nothing, required by the MaximoCache interface.
     */
    @Override
    public void init() throws MXException {
    }

    /**
     * Does nothing, required by the MaximoCache interface.
     */
    @Override
    public void reload() throws MXException {
        reload(null);
    }

    /**
     * Respond to a reload event and dispatch the print event on the local instance if required.
     */
    @Override
    public void reload(String key) throws MXException {

        if(LabelLogger.LABEL_LOGGER.isDebugEnabled()){
            LabelLogger.LABEL_LOGGER.debug("Received zebra label cache reload request for key: " + key);
        }

        if (key != null) {
            StringTokenizer tokens = new StringTokenizer(key, "|");
            // there should be 4 parts to the message; printer name, label name, object name, object Id.
            if (tokens.countTokens() == 4) {
                try {
                    String printerName = tokens.nextToken();
                    String labelName = tokens.nextToken();

                    String objectName = tokens.nextToken();
                    long objectId = Long.parseLong(tokens.nextToken());

                    MboRemote object = getObject(objectName, objectId);
                    if (object == null) {
                        LabelLogger.LABEL_LOGGER.error("Cannot find object for label print cache reload " + key);
                        return;
                    }

                    PrinterCfg printer = getPrinter(printerName);
                    if (printer == null) {
                        LabelLogger.LABEL_LOGGER.error("Cannot find printer address for label print cache reload " + key);
                        return;
                    }
                    String zpl = getLabel(labelName, object);

                    if (zpl == null) {
                        LabelLogger.LABEL_LOGGER.error("Cannot find label ZPL for label print cache reload " + key);
                        return;
                    }
                    EventMessage msg = new EventMessage(TOPIC, new LabelPrintEvent(printer.getAddress(), printer.getPort(), zpl));

                    if(LabelLogger.LABEL_LOGGER.isDebugEnabled()){
                        LabelLogger.LABEL_LOGGER.debug("Created event message for label print event " + msg.getEventObject());
                        LabelLogger.LABEL_LOGGER.debug("Posting event validation to the Maximo event topic tree: " + TOPIC);
                    }
                    MXServer.getEventTopicTree().eventValidate(TOPIC, msg);

                    if(LabelLogger.LABEL_LOGGER.isDebugEnabled()) {
                        LabelLogger.LABEL_LOGGER.debug("Posting event action to the Maximo event topic tree: " + TOPIC);
                    }

                    MXServer.getEventTopicTree().eventAction(TOPIC, msg);
                } catch (RemoteException e) {
                    LabelLogger.LABEL_LOGGER.error("A remote network error occurred while reloading the print label cache for " + key, e);
                }
            } else {
                LabelLogger.LABEL_LOGGER.error("The print label cache could not be reloaded for key " + key + " the key.  The key is not in the correct format.  Key format should be printerId|labelId|objectName|objectId");
            }
        }
    }

    /**
     * Returns the cache name, ZEBRALABEL.
     *
     * @return the cache name, ZEBRALABEL.
     */
    @Override
    public String getName() {
        return CACHE_NAME;
    }

    /**
     * Gets the configuration for the requested printer.
     *
     * @param printerName the name of the printer to retrieve.
     * @return the printer configuration object, or null if not found.
     * @throws RemoteException thrown if a remote networking error occurs.
     * @throws MXException     thrown if a Maximo application error occurs.
     */
    private PrinterCfg getPrinter(String printerName) throws RemoteException, MXException {
        ScriptInfo scriptInfo = ScriptCache.getInstance().getScriptInfo(PRINTERS_SCRIPT);
        ScriptCache.getInstance().reload();
        if (scriptInfo == null) {
            String[] params = new String[]{PRINTERS_SCRIPT};
            throw new MXApplicationException("script", "nosuchscript", params);
        } else {
            Map<String, Object> context = new HashMap<>();
            context.put("printerName", printerName);
            context.put("requestedPrinter", null);
            context.put("service", new ScriptService(PRINTERS_SCRIPT, null, null));

            ScriptDriverFactory.getInstance().getScriptDriver(PRINTERS_SCRIPT).runScript(PRINTERS_SCRIPT, context);
            if (context.containsKey("requestedPrinter")) {
                String printer = context.get("requestedPrinter").toString();
                return new Gson().fromJson(printer, PrinterCfg.class);
            } else {
                LabelLogger.LABEL_LOGGER.error("The printers config script " + PRINTERS_SCRIPT + " does not contain the required requestedPrinter variable.");
                return null;
            }
        }
    }

    /**
     * Gets the label ZPL for the provided label template name record object.
     *
     * @param labelName the name of the label template.
     * @param object    the Maximo object that will be used to substitute values in the label template.
     * @return the label ZPL.
     * @throws RemoteException thrown if a remote networking error occurs.
     * @throws MXException     thrown if a Maximo application error occurs.
     */
    private String getLabel(String labelName, MboRemote object) throws RemoteException, MXException {
        ScriptInfo scriptInfo = ScriptCache.getInstance().getScriptInfo(LABELS_SCRIPT);
        if (scriptInfo == null) {
            String[] params = new String[]{LABELS_SCRIPT};
            throw new MXApplicationException("script", "nosuchscript", params);
        } else {
            Map<String, Object> context = new HashMap<>();
            context.put("labelName", labelName);
            context.put("requestedLabel", null);
            context.put("service", new ScriptService(LABELS_SCRIPT, null, null));
            ScriptDriverFactory.getInstance().getScriptDriver(LABELS_SCRIPT).runScript(LABELS_SCRIPT, context);
            if (context.containsKey("requestedLabel")) {
                String label = context.get("requestedLabel").toString();
                String zpl = new Gson().fromJson(label, LabelCfg.class).getZpl();
                SqlFormat sqlf = new SqlFormat(object, zpl);
                sqlf.setIgnoreUnresolved(true);
                zpl = sqlf.resolveContent();

                return zpl.replace("\n", "").replace("\r", "");
            } else {
                LabelLogger.LABEL_LOGGER.error("The labels config script " + LABELS_SCRIPT + " does not contain a requestedLabel variable.");
                return null;
            }
        }
    }

    /**
     * Gets the Mbo for the provided object name and record Id.
     *
     * @param objectName the name of the Maximo object to get.
     * @param objectId   the unique Id for the Maximo record to get.
     * @return the Maximo object if found, or null if not.
     * @throws RemoteException thrown if a remote networking error occurs.
     * @throws MXException     thrown if a Maximo application error occurs.
     */
    private MboRemote getObject(String objectName, long objectId) throws RemoteException, MXException {
        MboSetRemote objectSet = null;
        try {
            objectSet = MXServer.getMXServer().getMboSet(objectName, MXServer.getMXServer().getSystemUserInfo());
            return objectSet.getMboForUniqueId(objectId);
        } finally {
            if (objectSet != null) {
                objectSet.cleanup();
                objectSet.close();
            }
        }
    }
}
