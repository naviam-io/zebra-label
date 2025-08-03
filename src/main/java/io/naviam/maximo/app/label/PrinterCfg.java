package io.naviam.maximo.app.label;

/**
 * Data object used for serializing the printer configuration with Gson.
 *
 * @author Jason VenHuizen
 */
public class PrinterCfg {

    private String address;
    private int port;

    /**
     * Default constructor required by Gson.
     */
    @SuppressWarnings("unused")
    public PrinterCfg() {
    }

    /**
     * Get the address for the printer.
     *
     * @return the address for the printer.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Set the address for the printer.
     *
     * @param address the address for the printer.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Get the port for the printer.
     *
     * @return the port for the printer.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port for the printer.
     *
     * @return the port for the printer.
     */
    public void setPort(int port) {
        this.port = port;
    }
}
