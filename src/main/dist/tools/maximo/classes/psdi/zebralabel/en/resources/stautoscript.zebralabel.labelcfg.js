/**
  This script contains definitions of the labels available to Opqo, for 
  printing inventory labels on Zebra printers.

  The available printers are defined in the script `STAUTOSCRIPT.ZEBRALABEL.PRINTERCFG`.

  Note that if the Naviam Maximo Zebra Printing extension is installed,
  labels can be defined and managed using the Maximo labels application that
  this adds.  In this case, any labels defined here will be merged with the 
  labels defined by the labels application (in the `STLABEL` table).

  This script defines a `labels` variable, containing an array of values with each
  value represents an available label. 

  Each object in the array of `labels` contains the following attributes:

  | Attribute   | Description                                                                                                                                                               |
  |:------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
  | label       | The unique identifier for the label.                                                                                                                                      |
  | description | A description of the label, displayed to the user.                                                                                                                        |
  | media       | The media identifier, typically in HxW (2x1, 4x6 etc) format although non-standard identifiers may be used.                                                               |
  | usewith     | The Maximo object that the label can be used with.  Opqo supports ITEM, INVENTORY and INVBALANCES.                                                                        |
  | zpl         | The ZPL definition for the label. The ZPL can contain Maximo bind variables (:ATTRIBUTE), such as :ITEMNUM that will be replaced with the value from the provided record. |
  | default     | Boolean value that indicates that the label is the default for the usewith and media combination.                                                                         |

  Key points to note:

  * The media field is used to identify the printers that can be used to print the label, 
    by finding printers with the same media value.
  * JSON does not allow line-breaks so the ZPL must be provided on a single line without line-breaks.
 
  When a user selects to print a label, the labels they see will be those that:

  * Have a `usewith` that matches the record being printed.
  * Have a printer (see `STAUTOSCRIPT.ZEBRALABEL.PRINTERCFG`) with a matching `media` value,
    that is in the user's current storeroom.
 
  Example configuration with a single label:

  var labels = [
    {
      "label": "EXAMPLELABEL",
      "description": "Example Label",
      "media": "4x2",
      "usewith": "INVBALANCES",
      "zpl": "^XA^FX Description of the item^CFA,30^FO50,50^FD:ITEM.DESCRIPTION^FS^FO50,120^GB700,3,3^FS^FX Section with bar code.^BY5,2,150^FO100,170^BC^FD:ITEMNUM^FS^XZ",
      "default": true
    }
  ];

*/

var labels = [];

var scriptConfig = {
  "autoscript": "STAUTOSCRIPT.ZEBRALABEL.LABELCFG",
  "description": "Barcode Label Configurations",
  "version": "1.2.0",
  "active": true,
  "logLevel": "ERROR"
};


