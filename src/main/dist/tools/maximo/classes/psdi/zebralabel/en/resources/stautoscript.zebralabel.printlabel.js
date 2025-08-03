var RuntimeException = Java.type("java.lang.RuntimeException");

var DataOutputStream = Java.type("java.io.DataOutputStream");
var IOException = Java.type("java.io.IOException");

var BindException = Java.type("java.net.BindException");
var InetSocketAddress = Java.type("java.net.InetSocketAddress");
var Socket = Java.type("java.net.Socket");
var SocketTimeoutException = Java.type("java.net.SocketTimeoutException");

var MboSetInfo = Java.type("psdi.mbo.MboSetInfo");
var SqlFormat = Java.type("psdi.mbo.SqlFormat");
var MXServer = Java.type("psdi.server.MXServer");
var MXApplicationException = Java.type("psdi.util.MXApplicationException");
var MXException = Java.type("psdi.util.MXException");
var MXLoggerFactory = Java.type("psdi.util.logging.MXLoggerFactory");

var logger = MXLoggerFactory.getLogger("maximo.naviam.zebra");

var timeout = 5000;

main();

function main() {
    if (typeof request !== "undefined" && request) {
        // if the request came from a REST call.
        var response = {};
        try {
            checkPermissions("STAUTOSCRIPT", "PRINTLABEL");

            var labelName = request.getQueryParam("label");
            var printerName = request.getQueryParam("printer");
            var objectName = request.getQueryParam("objectname");
            var recordId = request.getQueryParam("recordid");

            if ((!labelName || !printerName || !recordId || !objectName) && requestBody) {
                var params = JSON.parse(requestBody);
                labelName = params.label;
                printerName = params.printer;
                objectName = params.objectname;
                recordId = params.recordid;
            }

            if (!labelName) {
                throw new PrintError("missing_label", "A label parameter must be provided either as a query parameter or as part of the request body.");
            }

            if (!printerName) {
                throw new PrintError("missing_printer", "A printer parameter must be provided either as a query parameter or as part of the request body.");
            }

            if (!objectName) {
                throw new PrintError(
                    "missing_objectname",
                    "An objectname parameter must be provided either as a query parameter or as part of the request body."
                );
            }

            if (!recordId) {
                throw new PrintError("missing_recordid", "A recordid parameter must be provided either as a query parameter or as part of the request body.");
            }

            var printer = _getPrinterByName(printerName);
            var label = _getLabelByName(labelName);
            var record = _getRecordForObjectAndId(objectName, recordId);

            printLabel(label, printer, record);

            response.status = "success";
            responseBody = JSON.stringify(response);
        } catch (error) {
            response.status = "error";

            if (error instanceof PrintError) {
                response.message = error.message;
                response.reason = error.reason;
            } else if (error instanceof Error) {
                response.message = error.message;
            } else if (error instanceof MXException) {
                response.reason = error.getErrorGroup() + "_" + error.getErrorKey();
                response.message = error.getMessage();
            } else if (error instanceof SocketTimeoutException || error instanceof BindException) {
                response.message = error.getMessage();
                response.reason = "printer_connect_timeout";
            } else if (error instanceof IOException) {
                response.message = error.getMessage() + error.getClass().getName();
            } else if (error instanceof RuntimeException) {
                if (error.getCause() instanceof MXException) {
                    response.reason = error.getCause().getErrorGroup() + "_" + error.getCause().getErrorKey();
                    response.message = error.getCause().getMessage();
                } else {
                    response.reason = "runtime_exception";
                    response.message = error.getMessage();
                }
            } else {
                response.cause = error;
            }

            responseBody = JSON.stringify(response);
            service.log_error(error);

            return;
        }
    } else if (mbo && interactive) {
        // if the request came from the UI.
        var printer;
        var label;
        var record;

        if (mbo.isBasedOn("STPRINTLABEL")) {
            printer = _getPrinterByName(mbo.getString("PRINTER"));
            label = _getLabelByName(mbo.getString("LABEL"));
            record = _getRecordForObjectAndId(mbo.getString("PARENT"), mbo.getLong("PARENTID"));
        } else {
            var location;
            if (mbo.isBasedOn("INVBALANCES") || mbo.isBasedOn("INVENTORY") || mbo.isBasedOn("ASSET") || mbo.isBasedOn("LOCATIONS")) {
                location = mbo.getString("LOCATION");
            } else if (mbo.isBasedOn("MATRECTRANS")) {
                location = mbo.getString("TOSTORELOC");
            } else {
                logger.warn(
                    "The Mbo type " +
                        mbo.getName() +
                        " is not supported for label printing from the UI. Only STPRINTLABEL, INVBALANCES and MATRECTRANS are supported."
                );
                return;
            }

            printer = _getDefaultPrinterByLocation(location, mbo.getString("SITEID"));
            label = _getDefaultLabelForPrinter(printer);
            record = mbo;
        }

        try {
            printLabel(label, printer, record);
        } catch (error) {
            if (error instanceof PrintError) {
                throw new MXApplicationException("sharptree", "printError", Java.to([error.message], "java.lang.String[]"));
            } else if (error instanceof Error) {
                throw new MXApplicationException("sharptree", "printError", Java.to([error.message], "java.lang.String[]"));
            } else if (error instanceof MXException) {
                throw error;
            } else if (error instanceof SocketTimeoutException || error instanceof BindException) {
                throw new MXApplicationException("sharptree", "printError", Java.to([error.getMessage()], "java.lang.String[]"));
            } else if (error instanceof IOException) {
                throw new MXApplicationException("sharptree", "printError", Java.to([error.getMessage()], "java.lang.String[]"));
            } else if (error instanceof RuntimeException) {
                if (error.getCause() instanceof MXException) {
                    throw error.getCause();
                } else {
                    throw new MXApplicationException("sharptree", "printError", Java.to([error.getMessage()], "java.lang.String[]"));
                }
            } else {
                throw error;
            }
        }
    }
}

function printLabel(label, printer, record) {
    _validatePrinterLabelCombination(label, printer);
    _validatePrinterRecordSiteId(printer, record);
    _validateLabelUseWith(label, record);

    var clientSocket;

    try {
        if (printer.remote) {
            if (MXServer.getMXServer().getMaximoDD().getMboSetInfo("NVMPRINTQUEUE") != null) {
                var sqlf = new SqlFormat(record, label.zpl);
                sqlf.setIgnoreUnresolved(true);
                var zpl = sqlf.resolveContent();

                zpl = zpl.replace(/(\r\n|\n|\r)/gm, "");

                var localUserInfo = typeof userInfo === undefined ? userInfo : MXServer.getMXServer().getSystemUserInfo();
                var printQueueSet = MXServer.getMXServer().getMboSet("NVMPRINTQUEUE", localUserInfo);
                var agentSet = MXServer.getMXServer().getMboSet("NVMPRINTAGENT", localUserInfo);

                var agent = agentSet.moveFirst();

                while (agent) {
                    var printQueue = printQueueSet.add();
                    printQueue.setValue("ADDRESS", printer.address);
                    printQueue.setValue("PORT", printer.port);
                    printQueue.setValue("TIMEOUT", printer.timeout ? printer.timeout : timeout);
                    printQueue.setValue("ZPL", zpl);

                    printQueue.setValue("NVMPRINTAGENTID", agent.getUniqueIDValue());

                    agent = agentSet.moveNext();
                }
                printQueueSet.save();
            } else {
                MXServer.getMXServer().reloadMaximoCache(
                    "ZEBRALABEL",
                    printer.printer + "|" + label.label + "|" + record.getName() + "|" + record.getUniqueIDValue(),
                    true
                );
            }
        } else {
            var sqlf = new SqlFormat(record, label.zpl);
            sqlf.setIgnoreUnresolved(true);
            var zpl = sqlf.resolveContent();

            zpl = zpl.replace(/(\r\n|\n|\r)/gm, "");

            clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(printer.address, printer.port), printer.timeout ? printer.timeout : timeout);

            var outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeBytes(zpl);
            clientSocket.close();
        }
    } finally {
        if (clientSocket) {
            clientSocket.close();
        }
    }
}

function _getRecordForObjectAndId(objectName, recordId) {
    var recordSet;

    try {
        recordSet = MXServer.getMXServer().getMboSet(objectName, typeof userInfo === undefined ? userInfo : MXServer.getMXServer().getSystemUserInfo());
        var record = recordSet.getMboForUniqueId(recordId);

        if (!record) {
            throw new PrintError("record_not_found", objectName + " record for id " + recordId + " was not found.");
        }

        return record;
    } finally {
        close(recordSet);
    }
}

function _getDefaultPrinterByLocation(location, siteId) {
    var printers = service.invokeScript("STAUTOSCRIPT.ZEBRALABEL.PRINTERS");

    var printer;

    printers.printers.forEach(function (p) {
        if (p.location === location && p.siteid === siteId && p.default) {
            printer = p;
            return;
        }
    });

    if (!printer) {
        throw new PrintError("default_printer_not_configured", "A default printer for the location " + location + " at site " + siteId + " was not found.");
    }

    return printer;
}

function _getDefaultLabelForPrinter(printer) {
    var labels = service.invokeScript("STAUTOSCRIPT.ZEBRALABEL.LABELS");

    var label;
    labels.labels.forEach(function (l) {
        if (l.media == printer.media && l.default) {
            label = l;
            return;
        }
    });

    if (!label) {
        throw new PrintError("default_label_not_configured", "A default label for media type " + printer.media + " was not found.");
    }

    return label;
}

function _getPrinterByName(name) {
    var printers = service.invokeScript("STAUTOSCRIPT.ZEBRALABEL.PRINTERS");

    var printer;

    printers.printers.forEach(function (p) {
        if (p.printer === name) {
            printer = p;
            return;
        }
    });

    if (!printer) {
        if (typeof request === "undefined" || !request) {
            if (!name) {
                throw new MXException("sharptree", "noprinter", []);
            } else {
                throw new MXException("sharptree", "invalidPrinter", [name]);
            }
        } else {
            throw new PrintError("printer_not_configured", "Printer " + name + " is not configured.");
        }
    }

    return printer;
}

function _getLabelByName(name) {
    var labels = service.invokeScript("STAUTOSCRIPT.ZEBRALABEL.LABELS");

    var label;
    labels.labels.forEach(function (l) {
        if (l.label == name) {
            label = l;
            return;
        }
    });

    if (!label) {
        if (typeof request === "undefined" || !request) {
            if (!name) {
                throw new MXException("sharptree", "nolabel", []);
            } else {
                throw new MXException("sharptree", "invalidLabel", [name]);
            }
        } else {
            throw new PrintError("label_not_configured", "Label " + name + " is not configured.");
        }
    }

    return label;
}

function _validatePrinterLabelCombination(label, printer) {
    if (label.media != printer.media) {
        throw new PrintError("media_mismatch", "The printer media " + printer.media + " does not match the label's media of " + label.media);
    }
}

function _validateLabelUseWith(label, record) {
    if (!record.isBasedOn(label.usewith)) {
        throw new PrintError(
            "invalid_usewith",
            "The label " + label.label + " is to be used with the " + label.usewith + " object, which does not match the object of " + record.getName() + "."
        );
    }
}

function _validatePrinterRecordSiteId(printer, record) {
    var siteOrgType = MXServer.getMXServer().getMaximoDD().getMboSetInfo(record.getName()).getSiteOrgType();
    if (siteOrgType == MboSetInfo.SITELEVEL1 || siteOrgType == MboSetInfo.SYSTEMORGSITE9) {
        if (printer.siteid != record.getString("SITEID")) {
            throw new PrintError(
                "printer_siteid_mismatch",
                "The printer " + printer.printer + "'s site " + printer.siteid + " does not match the record's site of " + record.getString("SITEID") + "."
            );
        }
    }
}

function checkPermissions(app, optionName) {
    if (!userInfo) {
        throw new PrintError("no_user_info", "The userInfo global variable has not been set, therefore the user permissions cannot be verified.");
    }

    if (!MXServer.getMXServer().lookup("SECURITY").getProfile(userInfo).hasAppOption(app, optionName) && !isInAdminGroup()) {
        throw new PrintError(
            "no_permission",
            "The user " + userInfo.getUserName() + " does not have access to the " + optionName + " option in the " + app + " application."
        );
    }
}

// Determines if the current user is in the administrator group, returns true if the user is, false otherwise.
function isInAdminGroup() {
    var user = userInfo.getUserName();
    service.log_info("Determining if the user " + user + " is in the administrator group.");
    var groupUserSet;

    try {
        groupUserSet = MXServer.getMXServer().getMboSet("GROUPUSER", MXServer.getMXServer().getSystemUserInfo());

        // Get the ADMINGROUP MAXVAR value.
        var adminGroup = MXServer.getMXServer().lookup("MAXVARS").getString("ADMINGROUP", null);

        // Query for the current user and the found admin group.
        // The current user is determined by the implicity `user` variable.
        var sqlFormat = new SqlFormat("userid = :1 and groupname = :2");
        sqlFormat.setObject(1, "GROUPUSER", "USERID", user);
        sqlFormat.setObject(2, "GROUPUSER", "GROUPNAME", adminGroup);
        groupUserSet.setWhere(sqlFormat.format());

        if (!groupUserSet.isEmpty()) {
            service.log_info("The user " + user + " is in the administrator group " + adminGroup + ".");
            return true;
        } else {
            service.log_info("The user " + user + " is not in the administrator group " + adminGroup + ".");
            return false;
        }
    } finally {
        close(groupUserSet);
    }
}

// Cleans up the MboSet connections and closes the set.
function close(set) {
    if (set) {
        set.cleanup();
        set.close();
    }
}

function PrintError(reason, message) {
    Error.call(this, message);
    this.reason = reason;
    this.message = message;
}

// PrintError derives from Error
PrintError.prototype = Object.create(Error.prototype);
PrintError.prototype.constructor = PrintError;

var scriptConfig = {
    "autoscript": "STAUTOSCRIPT.ZEBRALABEL.PRINTLABEL",
    "description": "Print a Barcode Label",
    "version": "1.2.0",
    "active": true,
    "logLevel": "ERROR"
};
