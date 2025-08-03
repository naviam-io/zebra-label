/** 
  This script contains definitions of the Zebra printers available to Opqo, for 
  printing inventory labels.

  The available labels are defined in the script `STAUTOSCRIPT.ZEBRALABEL.LABELCFG`.

  Note that if the Sharptree Maximo Zebra Printing extension is installed, 
  printers can be defined and managed using the Maximo printers application that
  this adds.  In this case, any printers defined here will be merged with the 
  printers defined by the printers application (in the `STPRINTER` table).

  This script defines a `printers` variable, containing an array of values with each
  value represents an available printer. 

  Each object in the array of `printers` contains the following attributes:

  | Attribute   | Description                                                                                                             |
  |:------------|:------------------------------------------------------------------------------------------------------------------------|
  | printer     | The unique identifier for the printer.                                                                                  |
  | description | A description of the printer, displayed to the user.                                                                    |
  | address     | The network host name or IP address of the printer.                                                                     |
  | port        | The port that the printer is listening on, a value between 0-65535, defaults to 9100, which is the Zebra default port.  |
  | media       | The media identifier, typically in HxW (2x1, 4x6 etc) format although non-standard identifiers may be used.             |
  | default     | Boolean value that indicates that the printer is the default for the storeroom location.                                |
  | orgid       | The Maximo Organization identifier for the storeroom location.                                                          |
  | siteid      | The Maximo Site identifier for the storeroom location.                                                                  |
  | location    | The name of the storeroom location where the printer is located.                                                        |
  | remote      | Boolean value that indicates that the printer is remote to Maximo and is handled by the print agent.                    |

  Key points to note:

  * The `orgid`, `siteid` and `location` identify the storeroom location that the 
    printer resides in.  This storeroom location is used to filter the printers 
    that are available to those that match the record being printed.
  * The `media` field is used to identify the labels that can be printed on the printer, 
    by finding labels with the same media value.

  Hence, when a user selects to print a label, the printers they see will be the printers that:

  * Have a storeroom location that matches the user's current storeroom.
  * Have a label (see `STAUTOSCRIPT.ZEBRALABEL.LABELCFG`) with a matching `media` value
    and a `usewith` that matches the record being printed.

  Example configuration with a single printer:

  var printers = [
    {
      "printer": "EXAMPLEPRINTER",
      "description": "Example Printer",
      "address": "central-printer.acme.com",
      "port": 9100,
      "media": "4x2",
      "default": true,
      "orgid": "EAGLENA",
      "siteid": "BEDFORD",
      "location": "CENTRAL",
      "remote": false
    }
  ];

*/

var printers = [];

var scriptConfig = {
  "autoscript": "STAUTOSCRIPT.ZEBRALABEL.PRINTERCFG",
  "description": "Barcode Printer Configurations",
  "version": "1.2.0",
  "active": true,
  "logLevel": "ERROR"
};