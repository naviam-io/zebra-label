var System = Java.type("java.lang.System");
var Thread = Java.type("java.lang.Thread");

var DBShortcut = Java.type("psdi.mbo.DBShortcut");
var SqlFormat = Java.type("psdi.mbo.SqlFormat");

var MXServer = Java.type("psdi.server.MXServer");
var MXLoggerFactory = Java.type("psdi.util.logging.MXLoggerFactory");

var logger = MXLoggerFactory.getLogger("maximo.naviam.zebra");

var TOPIC = "naviam.printlabel";
var shutdown = false;
var outputStream = null;
var agentId = -1;

// ServerSentEvent object to represent a Server Sent Event. The sse function formats the event as a UTF-8 byte array for sending to the client
function ServerSentEvent(id, event, data) {
    var JavaString = Java.type("java.lang.String");
    this.id = id;
    this.event = event;
    this.data = data;
    this.sse = function () {
        return new JavaString("id: " + this.id + "\n" + "event: " + this.event + (data ? "\n" + "data: " + this.data : "") + "\n\n").getBytes("UTF-8");
    };
}

// Nashorn is ES5 compliant so we need to define the prototype for the ServerSentEvent object
// create a new prototype object for the ServerSentEvent object
ServerSentEvent.prototype = Object.create(Object.prototype);
// assign the ServerSentEvent function as the constructor for the ServerSentEvent prototype
ServerSentEvent.prototype.constructor = ServerSentEvent;

// Create a new runnable thread to perform the migration process.
var PrintLabelEventListener = Java.extend(Java.type("psdi.server.event.EventListener"), {
    eventValidate: function (event) {
        var obj = event.getEventObject();
        if (obj !== null && typeof obj.getPrinter !== "undefined") {
            var data = {
                "printer": obj.getPrinter(),
                "port": obj.getPort(),
                "label": obj.getLabel()
            };

            logger.debug("Received print label event from the Maximo Cache Reload:\n" + JSON.stringify(data));

            // Create a new ServerSentEvent object as defined at the bottom of the script.
            var sseEvent = new ServerSentEvent(System.currentTimeMillis(), "print", JSON.stringify(data));
            // Call the sse() function on the sseEvent object to generate a SSE formatted String and convert that to a UTF-8 byte array.
            try {
                outputStream.write(sseEvent.sse());
                outputStream.flush();
            } catch (error) {
                logger.info(
                    "An error occurred while writing to the output stream using the Maximo Cache Reload listener: " +
                        (typeof error.getMessage !== "undefined" ? error.getMessage() : error) +
                        "\nShutting down the Zebra Label Dispatch script due to the error."
                );

                // If an error occurs while writing to the output stream, set shutdown to true to stop the event loop.
                shutdown = true;
            }
        }
    },
    preSaveEventAction: function (event) {},
    eventAction: function (event) {},
    postCommitEventAction: function (event) {}
});

main();

function main() {
    if (typeof request !== "undefined") {
        // get a reference to the HTTPServletResponse object.
        var response = request.getHttpServletResponse();

        // set the buffer to zero so messages are immediately sent to the client.
        response.setBufferSize(0);
        // set the response type as text/event-stream to indicate that an event stream is being sent.
        response.setContentType("text/event-stream");
        // indicate that the connection should be kept alive
        response.addHeader("Connection", "keep-alive");
        // indicate to the client that the cache should not be used.
        response.addHeader("Cache-Control", "no-cache");

        // flush the buffer to send and commit the headers
        response.flushBuffer();

        // get the output stream for the response.
        outputStream = response.getOutputStream();

        var listenerId = -1;
        var agentId = -1;

        // If the NVMPRINTQUEUE table exists, then we will use it to send print jobs to the printers.
        if (MXServer.getMXServer().getMaximoDD().getMboSetInfo("NVMPRINTQUEUE") != null) {
            logger.debug("Running Zebra Label Dispatch script with NVMPRINTQUEUE support.");

            // Register the agent to keep track of the active agents.
            agentId = registerAgent();

            if (agentId === -1) {
                logger.error("Could not register the agent in the NVMPRINTAGENT table. The Zebra Label Dispatch script will not run.");
                return;
            }

            var thread = new Thread(function () {
                try {
                    var dbShortcut = new DBShortcut();

                    var connectionKey = userInfo.getConnectionKey();

                    var sqlf = new SqlFormat("select address, port, zpl, timeout, nvmprintqueueid from nvmprintqueue where nvmprintagentid = :1");

                    sqlf.setLong(1, agentId);

                    var sql = sqlf.format();

                    try {
                        dbShortcut.connect(connectionKey);
                        while (!shutdown) {
                            var result = dbShortcut.executeQuery(sql);

                            if (result.next()) {
                                var data = {
                                    "address": result.getString("ADDRESS"),
                                    "port": result.getInt("PORT"),
                                    "zpl": result.getString("ZPL"),
                                    "timeout": result.getInt("TIMEOUT")
                                };

                                logger.debug(
                                    "The NVMPRINTQUEUE table has a print job for agent " +
                                        agentId +
                                        ". Sending data to agent to print label:\n" +
                                        JSON.stringify(data, null, 4)
                                );

                                var queueId = result.getLong(5);

                                var sseEvent = new ServerSentEvent(queueId, "print", JSON.stringify(data));

                                // Call the sse() function on the sseEvent object to generate a SSE formatted String and convert that to a UTF-8 byte array.
                                try {
                                    outputStream.write(sseEvent.sse());
                                    outputStream.flush();
                                    var deleteSql = new SqlFormat("delete from nvmprintqueue where nvmprintqueueid = :1");
                                    deleteSql.setLong(1, result.getLong("NVMPRINTQUEUEID"));
                                    dbShortcut.execute(deleteSql);
                                    dbShortcut.commit();
                                } catch (error) {
                                    // If an error occurs while writing to the output stream, set shutdown to true to stop the event loop.
                                    logger.info(
                                        "An error occurred while writing to the output stream using the NVMPRINTQUEUE: " +
                                            (typeof error.getMessage !== "undefined" ? error.getMessage() : error) +
                                            "\nShutting down the Zebra Label Dispatch script due to the error."
                                    );
                                    shutdown = true;
                                }
                            }

                            // Close the result set to free up resources.
                            result.close();

                            try {
                                var interval = getPropertyAsInt("naviam.zebra.pollInterval", 5000, true, true);
                                if (interval < 1000) {
                                    interval = interval * 1000; // If the interval is less than a second then multiply to 1000 to convert to seconds.
                                }
                                Thread.sleep(interval); // Sleep for the specified interval before checking the queue again.
                            } catch (ignored) {
                                // The sleep was interrupted, which is not a problem for the script, just try again and if something is actually wrong allow the shutdown to occur.
                                logger.debug("Sleep interrupted, continuing to check the NVMPRINTQUEUE.");
                            }
                        }
                    } finally {
                        dbShortcut.close();
                    }
                } catch (ignored) {
                    logger.info(
                        "An error occurred while monitoring the NVMPRINTQUEUE queue table: " +
                            (typeof error.getMessage !== "undefined" ? error.getMessage() : error) +
                            "\nShutting down the Zebra Label Dispatch script due to the error."
                    );
                    shutdown = true;
                }
            });
            thread.start();
        } else {
            // Get the current MboSet and Mbo from the event.
            logger.warn("Running Zebra Label Dispatch script without NVMPRINTQUEUE support, using the Maximo reload cache, which may impact performance.");
            listenerId = MXServer.getEventTopicTree().register(TOPIC, new PrintLabelEventListener());
        }
        try {
            // Wait for the shutdown signal.
            while (!shutdown) {
                var interval = getPropertyAsInt("naviam.zebra.pingInterval", 10000, true, true);
                if (interval < 1000) {
                    interval = interval * 1000; // If the interval is less than a second then multiply to 1000 to convert to seconds.
                }

                logger.debug("Waiting for " + interval + " milliseconds before sending the next ping event.");

                // Sleep for a short duration to avoid busy waiting.
                Thread.sleep(interval);

                // Create a new ServerSentEvent object as defined at the bottom of the script.
                var sseEvent = new ServerSentEvent(System.currentTimeMillis(), "ping", null);
                // Call the sse() function on the sseEvent object to generate a SSE formatted String and convert that to a UTF-8 byte array.
                try {
                    outputStream.write(sseEvent.sse());
                    outputStream.flush();
                } catch (error) {
                    // If an error occurs while writing to the output stream, set shutdown to true to stop the event loop.
                    shutdown = true;
                    logger.info(
                        "An error occurred while monitoring the connection with agent with ID " +
                            agentId +
                            ": " +
                            (typeof error.getMessage !== "undefined" ? error.getMessage() : error) +
                            "\nShutting down the Zebra Label Dispatch script due to the error."
                    );
                }

                if (!shutdown) {
                    // update the agent date to indicate that the agent is still active.
                    // return false if the agent is not found, which will cause the event loop to stop.
                    shutdown = !updateAgentDate(agentId);

                    if (shutdown) {
                        logger.info("The agent with ID " + agentId + " was not found, shutting down the Zebra Label Dispatch script.");
                    }
                }
            }
        } finally {
            if (listenerId !== -1) {
                MXServer.getEventTopicTree().unregister(TOPIC, listenerId);
            }
            removeAgent(agentId);
        }
    } else {
        logger.debug(
            "The NAVIAM.ZEBRALABEL.DISPATCH script was called from a context without a request object in the context. This is likely due to the script being run in a non-web context, such as a scheduled job or a script execution from a Maximo action, which is not a supported use case."
        );
    }
}

/**
 * Get a property as an integer value from the Maximo server properties.
 * @param {string} propertyName the name of the property to retrieve.
 * @param {string} defaultValue the default value to return if the property is not set or is invalid.
 * @param {boolean} isPositive whether the property value must be a positive integer.
 * @param {boolean} isGreaterThanZero whether the property value must be greater than zero.
 * @returns an integer value of the property, or the default value if the property is not set or is invalid.
 */
function getPropertyAsInt(propertyName, defaultValue, isPositive, isGreaterThanZero) {
    if (defaultValue == null) {
        throw new Error("Default value must be specified for property: " + propertyName);
    }

    // default value to being a positive integer if not specified
    if (isPositive == null) {
        isPositive = true;
    }
    if (isGreaterThanZero == null) {
        isGreaterThanZero = false;
    }

    var returnValue = defaultValue; // 10 seconds
    try {
        var sValue = null;
        try {
            sValue = MXServer.getMXServer().getProperty(propertyName);
        } catch (ignored) {
            // Ignore the error because there is nothing we can do about it when trying to get the property.
            logger.debug("Could not read the " + propertyName + " property, using default value of " + defaultValue + ".");
        }

        if (sValue !== null) {
            returnValue = parseInt(sValue, 10);
            if (isNaN(returnValue) || (isPositive && returnValue < 0) || (isGreaterThanZero && returnValue <= 0)) {
                returnValue = defaultValue; // Default to 10 seconds if the value is invalid.
            }
        }
    } catch (ignored) {
        // Ignore parsing errors when reading the timeout value and just use the default interval.
    }
    return returnValue;
}

/**
 * Remove an agent from the NVMPRINTAGENT table.
 * This will also remove all print jobs associated with the agent from the NVMPRINTQUEUE table
 * @param {int} id the unique ID of the agent to remove.
 * @returns {boolean} true if the agent was removed successfully, false otherwise.
 */
function removeAgent(id) {
    if (MXServer.getMXServer().getMaximoDD().getMboSetInfo("NVMPRINTAGENT") != null) {
        var printAgentSet = MXServer.getMXServer().getMboSet("NVMPRINTAGENT", userInfo);
        try {
            var printAgent = printAgentSet.getMboForUniqueId(id);
            if (printAgent != null) {
                var sqlf = new SqlFormat("nvmprintagentid = :1");
                sqlf.setLong(1, id);
                printAgent.getMboSet("$printqueue", "NVMPRINTQUEUE", sqlf.format()).deleteAll();
                printAgent.delete();
                printAgentSet.save();
                return true;
            }
        } finally {
            try {
                printAgentSet.close();
                printAgentSet.cleanup();
            } catch (ignored) {
                // Ignore the error because there is nothing we can do about it when trying to close the MboSet.
            }
        }
    }

    return false;
}

/**
 * Update the last ping date of an agent.
 * @param {int} id the unique ID of the agent to update.
 * @returns {boolean} true if the agent was updated successfully, false otherwise.
 */
function updateAgentDate(id) {
    if (MXServer.getMXServer().getMaximoDD().getMboSetInfo("NVMPRINTAGENT") != null) {
        var printAgentSet = MXServer.getMXServer().getMboSet("NVMPRINTAGENT", userInfo);
        try {
            var printAgent = printAgentSet.getMboForUniqueId(id);

            if (printAgent != null) {
                printAgent.setValue("LASTPINGDATE", MXServer.getMXServer().getDate());
                printAgentSet.save();
                return true;
            } else {
                return false;
            }
        } finally {
            try {
                printAgentSet.close();
                printAgentSet.cleanup();
            } catch (ignored) {
                // Ignore the error because there is nothing we can do about it when trying to close the MboSet.
            }
        }
    } else {
        return true;
    }
}

/**
 * Register a new agent in the NVMPRINTAGENT table.
 * This will create a new agent with the current date as the created date and last ping date.
 * The agent will be used to track the active agents and their last ping date.
 * @returns {int} the unique ID of the agent that was registered, or -1 if the agent could not be registered.
 */
function registerAgent() {
    if (MXServer.getMXServer().getMaximoDD().getMboSetInfo("NVMPRINTAGENT") != null) {
        var printAgentSet = MXServer.getMXServer().getMboSet("NVMPRINTAGENT", userInfo);
        try {
            var printAgent = printAgentSet.add();
            printAgent.setValue("CREATEDATE", MXServer.getMXServer().getDate());
            printAgent.setValue("LASTPINGDATE", MXServer.getMXServer().getDate());
            printAgentSet.save();

            return printAgent.getUniqueIDValue();
        } finally {
            try {
                printAgentSet.close();
                printAgentSet.cleanup();
            } catch (ignored) {
                // Ignore the error because there is nothing we can do about it when trying to close the MboSet.
            }
        }
    } else {
        return -1;
    }
}

var scriptConfig = {
    "autoscript": "NAVIAM.ZEBRALABEL.DISPATCH",
    "description": "Barcode Printer Remote Agent Dispatch",
    "version": "1.2.0",
    "active": true,
    "logLevel": "ERROR"
};
