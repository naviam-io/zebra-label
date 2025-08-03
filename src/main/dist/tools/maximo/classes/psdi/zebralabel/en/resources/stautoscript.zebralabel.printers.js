var printers = service.invokeScript("STAUTOSCRIPT.ZEBRALABEL.PRINTERCFG").printers;

var MXServer = Java.type("psdi.server.MXServer");
var SqlFormat = Java.type("psdi.mbo.SqlFormat");
var MXLoggerFactory = Java.type("psdi.util.logging.MXLoggerFactory");

var logger = MXLoggerFactory.getLogger("maximo.naviam.zebra");

main();

function main() {
    if (logger.isDebugEnabled()) {
        logger.debug("Getting the Zebra Label Printers");
    }

    _mergePrinters();

    printers = _validateAndRemoveInvalidPrinters(printers);

    if (typeof request !== "undefined" && request) {
        responseBody = JSON.stringify(printers);
    } else if (typeof printerName !== "undefined" && printerName) {
        printers.forEach(function (printer) {
            if (printer.printer == printerName) {
                requestedPrinter = JSON.stringify(printer);
            }
        });
    }
}

function _mergePrinters() {
    if (MXServer.getMXServer().getMaximoDD().getMboSetInfo("STPRINTER")) {
        if (logger.isDebugEnabled()) {
            logger.debug("The Zebra Label Maximo application has been installed, merging Maximo configuration with values from the script.");
        }

        var printerSet;

        try {
            printerSet = MXServer.getMXServer().getMboSet("STPRINTER", MXServer.getMXServer().getSystemUserInfo());
            if (!printerSet.isEmpty()) {
                var printer = printerSet.moveFirst();

                while (printer) {
                    var printerConfig = _convertMboToObject(printer);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Got printer configuration from Maximo:\n" + JSON.stringify(printerConfig, null, 4));
                    }

                    printers.push(printerConfig);
                    printer = printerSet.moveNext();
                }
            }
        } finally {
            close(printerSet);
        }
    } else {
        if (logger.isDebugEnabled()) {
            logger.debug("The Zebra Label Maximo application has not been installed, only returning values from the script.");
        }
    }
}

function _validateAndRemoveInvalidPrinters(printers) {
    var tmpPrinters = [];
    printers.forEach(function (printer) {
        if (_validatePrinterAttributes(printer) && _validateMedia(printer) && _validateSiteId(printer) && _validateLocation(printer)) {
            printer.printer = printer.printer.toUpperCase();
            printer.location = printer.location.toUpperCase();
            printer.siteid = printer.siteid.toUpperCase();
            printer.remote = typeof printer.remote == "undefined" || !printer.remote ? false : true;
            tmpPrinters.push(printer);
        } else {
            logger.error(
                "The following printer configuration is missing either a printer, address port, storeroom, media or siteid attribute and will not be returned. \n\n" +
                    JSON.stringify(printer, null, 4) +
                    "\n"
            );
        }
    });

    return tmpPrinters;
}

function _validatePrinterAttributes(printer) {
    return printer.printer && printer.address && printer.port && printer.location && printer.media && printer.siteid;
}

function _validateSiteId(printer) {
    var siteSet;

    try {
        var sqlf = new SqlFormat(" siteid = :1");
        sqlf.setObject(1, "SITE", "SITEID", printer.siteid);

        siteSet = MXServer.getMXServer().getMboSet("SITE", MXServer.getMXServer().getSystemUserInfo());
        siteSet.setWhere(sqlf.format());

        var result = !siteSet.isEmpty();
        if (!result) {
            logger.error(
                "The printer site " +
                    printer.siteid +
                    " is not a valid Maximo site. The complete invalid printer configuration is:\n\n" +
                    JSON.stringify(printer, null, 4) +
                    "\n"
            );
        }

        return result;
    } finally {
        close(siteSet);
    }
}

function _validateLocation(printer) {
    var locationSet;

    try {
        var sqlf = new SqlFormat("location = :1 and siteid = :2");
        sqlf.setObject(1, "LOCATIONS", "LOCATION", printer.location);
        sqlf.setObject(2, "LOCATIONS", "SITEID", printer.siteid);

        locationSet = MXServer.getMXServer().getMboSet("LOCATIONS", MXServer.getMXServer().getSystemUserInfo());
        locationSet.setWhere(sqlf.format());

        var result = !locationSet.isEmpty();
        if (!result) {
            logger.error(
                "The printer location " +
                    printer.location +
                    " is not a valid location for the " +
                    printer.siteid +
                    " site. The complete invalid printer configuration is:\n\n" +
                    JSON.stringify(printer, null, 4) +
                    "\n"
            );
        }

        return result;
    } finally {
        close(locationSet);
    }
}

function _validateMedia(printer) {
    if (printer.media) {
        var mboSetInfo = MXServer.getMXServer().getMaximoDD().getMboSetInfo("STPRINTER");
        if (mboSetInfo) {
            var domainId = mboSetInfo.getMboValueInfo("MEDIA").getDomainId();
            var sqlf = new SqlFormat("domainid = :1 and value = :2");
            sqlf.setObject(1, "ALNDOMAIN", "DOMAINID", domainId);
            sqlf.setObject(2, "ALNDOMAIN", "VALUE", printer.media);

            var alnDomainSet;

            try {
                alnDomainSet = MXServer.getMXServer().getMboSet("ALNDOMAIN", MXServer.getMXServer().getSystemUserInfo());
                alnDomainSet.setWhere(sqlf.format());
                return !alnDomainSet.isEmpty();
            } finally {
                close(alnDomainSet);
            }
        } else {
            return true;
        }
    }
    return false;
}

function _convertMboToObject(mbo) {
    var printer = {};

    printer.printer = mbo.getString("PRINTER");
    printer.description = mbo.getString("DESCRIPTION");
    printer.address = mbo.getString("ADDRESS");
    printer.port = mbo.getInt("PORT");
    printer.location = mbo.getString("LOCATION");
    printer.media = mbo.getString("MEDIA");
    printer.default = mbo.getBoolean("ISDEFAULT");
    printer.siteid = mbo.getString("SITEID");
    printer.orgid = mbo.getString("ORGID");
    printer.remote = mbo.getBoolean("REMOTE");

    return printer;
}

function close(set) {
    if (set) {
        set.cleanup();
        set.close();
    }
}

var scriptConfig = {
    "autoscript": "STAUTOSCRIPT.ZEBRALABEL.PRINTERS",
    "description": "Barcode Printers",
    "version": "1.2.0",
    "active": true,
    "logLevel": "ERROR"
};
