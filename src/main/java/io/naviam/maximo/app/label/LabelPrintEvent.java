package io.naviam.maximo.app.label;

/**
 * Event object that represents a label that needs to be printed by a remote printer.
 *
 * @author Jason VenHuizen
 */
public class LabelPrintEvent {
    int port;
    String printer;
    String label;

    /**
     * Creates a new LabelPrintEvent with the printer, port, and label to print.
     *
     * @param printer the printer hostname or IP address.
     * @param port    the port that the printer is listening on.
     * @param label   the complete label ZPL with values replaced.
     */
    public LabelPrintEvent(String printer, int port, String label) {
        this.port = port;
        this.printer = printer;
        this.label = label;
    }

    /**
     * Gets the port number for the printer.
     *
     * @return the port number for the printer.
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the hostname or IP for the printer.
     *
     * @return the hostname or IP for the printer.
     */
    public String getPrinter() {
        return printer;
    }

    /**
     * Gets the ZPL for the label.
     *
     * @return the ZPL for the label.
     */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "printer:" + printer + " port:" + port + " label:" + label;
    }


}
