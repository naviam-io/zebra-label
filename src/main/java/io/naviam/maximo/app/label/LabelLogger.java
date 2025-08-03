package io.naviam.maximo.app.label;

import psdi.util.logging.MXLogger;
import psdi.util.logging.MXLoggerFactory;

/**
 * Interface that provides the label logger, similar to the psdi.util.logging.FixedLoggers
 *
 * @author Jason VenHuizen
 */
public interface LabelLogger {
    MXLogger LABEL_LOGGER = MXLoggerFactory.getLogger("maximo.naviam.label");
}
