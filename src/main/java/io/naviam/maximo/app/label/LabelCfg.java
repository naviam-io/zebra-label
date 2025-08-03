package io.naviam.maximo.app.label;

/**
 * Data object used for serializing the label configuration with Gson.
 *
 * @author Jason VenHuizen
 */
public class LabelCfg {

    private String zpl;

    /**
     * Required default constructor for Gson serialization.
     */
    @SuppressWarnings("unused")
    public LabelCfg() {
    }

    /**
     * Gets the ZPL for the label.
     *
     * @return the ZPL for the label.
     */
    public String getZpl() {
        return zpl;
    }

    /**
     * Set the ZPL for the label.
     *
     * @param zpl the ZPL for the label.
     */
    public void setZpl(String zpl) {
        this.zpl = zpl;
    }
}

