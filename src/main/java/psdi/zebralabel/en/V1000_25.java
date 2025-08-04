package psdi.zebralabel.en;

import psdi.script.AutoUpgradeTemplate;

import java.io.PrintStream;
import java.sql.Connection;
import java.util.HashMap;

/**
 * Update the required automation scripts for the Zebra Label printing for remote printing.
 *
 * @author Jason VenHuizen
 */

@SuppressWarnings("unused")
public class V1000_25 extends AutoUpgradeTemplate {

    /**
     * Creates a new instance of the V1000_15 class.
     *
     * @param con    the database connection.
     * @param params the parameter provided.
     * @param ps     the output stream for logging statements.
     * @throws Exception thrown if an error occurs creating the scripts.
     */
    public V1000_25(Connection con, HashMap params, PrintStream ps) throws Exception {
        super(con, params, ps);
    }

    /**
     * Sets the scriptFileName to `V1000_15`
     *
     * @throws Exception throw if an error occurs while initializing the upgrade template.
     */
    public void init() throws Exception {
        this.scriptFileName = "V1000_25";
        super.init();
    }

    @Override
    protected void process() throws Exception {
        AutoScriptUtil.createOrUpdateScript(con, "NAVIAM.ZEBRALABEL.DISPATCH", "psdi/zebralabel/en/resources/naviam.zebralabel.dispatch.js", "Naviam Zebra Label Printing Dispatch", "1.3.0", dbIn);
        AutoScriptUtil.createOrUpdateScript(con, "STAUTOSCRIPT.ZEBRALABEL.LABELS", "psdi/zebralabel/en/resources/stautoscript.zebralabel.labels.js", "Barcode Label Definitions", "1.3.0", dbIn);
        AutoScriptUtil.createOrUpdateScript(con, "STAUTOSCRIPT.ZEBRALABEL.PRINTERS", "psdi/zebralabel/en/resources/stautoscript.zebralabel.printers.js", "Barcode Printers", "1.3.0", dbIn);
        AutoScriptUtil.createOrUpdateScript(con, "STAUTOSCRIPT.ZEBRALABEL.PRINTLABEL", "psdi/zebralabel/en/resources/stautoscript.zebralabel.printlabel.js", "Print a Barcode Label", "1.3.0", dbIn);

        super.process();
    }
}
