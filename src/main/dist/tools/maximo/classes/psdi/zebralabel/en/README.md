# Introduction 
The Zebra Label Printing automation scripts provide the ability to define uniquely identified printers and labels, and then print labels by providing the printer and label names as well as a object name and object unique identifier.  

Bind variables specified in the label ZPL will be replaced with values from the Maximo object, and the label will be printed by sending the ZPL to the specified Zebra ZPL compatible network printer.

## Shared Components
The automation scripts provided in this repository are used by both Opqo [https://opqo.io](https://opqo.io) and the Maximo Zebra Printing extension [https://github.com/sharptree/zebra-label](https://github.com/sharptree/zebra-label).

When used by Opqo or other standard alone implementations the printer and label configuration will be taken from the `stautoscript.zebralabel.printercfg` and `stautoscript.zebralabel.labelcfg` scripts respectively.  

When the Maximo Zebra Printing extension is installed printer and label configurations will combine what is configured in the `stautoscript.zebralabel.printercfg` and `stautoscript.zebralabel.labelcfg` scripts with the configuration in the `STPRINTER` and `STLABEL` tables.

This document only covers configuring printers and labels via the `stautoscript.zebralabel.printercfg` and `stautoscript.zebralabel.labelcfg` automation scripts.

# Printers
Printers are defined as an array of values assigned to the `printers` variable in the `stautoscript.zebralabel.printercfg` automation script.

Each object in the array of printers contains the following attributes:

| Attribute    | Description                                                                                                             |
|:-------------|:------------------------------------------------------------------------------------------------------------------------|
| printer      | The unique identifier for the printer.                                                                                  |
| description  | A description of the printer, displayed to the user.                                                                    |
| address      | The network host name or IP address of the printer.                                                                     |
| port         | The port that the printer is listening on, a value between 0-65535, defaults to 9100, which is the Zebra default port.  |
| media        | The media identifier, typically in HxW (2x1, 4x6 etc) format although non-standard identifiers may be used.             |
| default      | Boolean value that indicates that the printer is the default for the storeroom location.                                |
| orgid        | The Maximo Organization identifier for the storeroom location.                                                          |
| siteid       | The Maximo Site identifier for the storeroom location.                                                                  |
| location     | The name of the storeroom location where the printer is located.                                                        |
| remote       | Boolean value that indicates that the printer is remote to Maximo and is handled by the print agent.                    |
Key points to note:

* The orgid, siteid and location identify the storeroom location that the printer resides in.  
  This storeroom location is used to filter the printers that are available to those that match the record being printed.
* The media field is used to identify the labels that can be printed on the printer, by finding labels with the same media value.

## Example Printer

```json
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
```

# Labels
All labels are defined as an array of values assigned to the `labels` variable in the `stautoscript.zebralabel.labelcfg` automation script.  

Each object in the array of labels contains the following attributes:

| Attribute   | Description                                                                                                                                                               |
|:------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| label       | The unique identifier for the label.                                                                                                                                      |
| description | A description of the label, displayed to the user.                                                                                                                        |
| media       | The media identifier, typically in HxW (2x1, 4x6 etc) format although non-standard identifiers may be used.                                                               |
| usewith     | The Maximo object that the label can be used with.  INVBALANCES, INVENTORY and MATRECTRANS are supported.                                                                 |
| zpl         | The ZPL definition for the label. The ZPL can contain Maximo bind variables (:ATTRIBUTE), such as :ITEMNUM that will be replaced with the value from the provided record. |
| default     | Boolean value that indicates that the label is the default for the usewith and media combination.                                                                         |

Key points to note:

* The media field is used to identify the printers that can be used to print the label, by finding printers with the same media value.

## Example Label

```json
  {
    "label": "EXAMPLELABEL",
    "description": "Example Label",
    "media": "4x2",
    "usewith": "INVBALANCES",
    "zpl": "^XA^FX Description of the item^CFA,30^FO50,50^FD:ITEM.DESCRIPTION^FS^FO50,120^GB700,3,3^FS^FX Section with bar code.^BY5,2,150^FO100,170^BC^FD:ITEMNUM^FS^XZ",
    "default": true
  }
```
### ZPL
JSON does not allow line-breaks so the ZPL must be provided on a single line without line-breaks.  For clarity the ZPL is provided formatted below.
Note the use of :ITEM.DESCRIPTION and :ITEMNUM Maximo bind variables that are replaced with the current Maximo record's values.

```zpl
^XA

^FX Description of the item
^CFA,30
^FO50,50^FD:ITEM.DESCRIPTION^FS
^FO50,120^GB700,3,3^FS

^FX Section with bar code.
^BY5,2,150
^FO100,170^BC^FD:ITMENUM^FS

^XZ
```
When used with the INVBALANCES record for item number *11453* the following label is printed.

![Simple Inventory Balances Label](images/invbalances_2x4.png)

>A full reference for the ZPL commands can be found here: [https://www.zebra.com/content/dam/zebra/manuals/printers/common/programming/zpl-zbi2-pm-en.pdf](https://www.zebra.com/content/dam/zebra/manuals/printers/common/programming/zpl-zbi2-pm-en.pdf)
> 
>Labelary provides an excellent tool for creating and editing ZPL labels here: [http://labelary.com/viewer.html](http://labelary.com/viewer.html) .

# Printing Labels
## Security
To print a label the requesting user must be either be part of the Maximo administrators group, defined in the `ADMINGROUP` `MAXVAR` entry, or have been granted the `PRINTLABEL` signature security option on the `STAUTOSCRIPT` integration object.  

## HTTP Query Parameters

To print a label, make an HTTP request to the `stautoscript.zebralabel.printlabel` automation script with the following query parameters:

| Query Parameter | Description                                                                                                                                                                                                  |
|:----------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| labelName       | The unique identifier for the label configured in the `stautoscript.zebralabel.labelcfg` script or in the `STLABEL` table.  The label's media attribute must match the selected printer's media type.        |
| printerName     | The unique identifier for the printer configured in the `stautoscript.zebralabel.printercfg` script or in the `STPRINTER` table.  The printers's media attribute must match the selected label's media type. |
| objectName      | The name of the Maximo object that will be used for bind variable replacement.                                                                                                                               |
| recordId        | The Maximo unique record identifier for the provided object.                                                                                                                                                 |

### Example
```url
https://maximo-host.acme.com/maximo/oslc/script/stautoscript.zebralabel.printlabel?labelName=MYLABEL&printerName=MYPRINTER&objectName=INVBALANCES&recordId=123456
```

## HTTP Request Body

If any of the query parameters are not provided the script expects that a request payload is provided and all query parameters are ignored. The payload is a JSON object with the same attributes as the query parameters.

### Example
```json
{
  "labelName": "MYLABEL",
  "printerName": "MYPRINTER",
  "objectName": "INVBALANCES",
  "recordId": 123456
}
```

The script allows a request body using either the `GET` or `POST` HTTP method, although it is preferred to use `POST` as this is aligns with spirit of the HTTP specification.
> A discussion of HTTP GET requests with request bodies can be found here: [https://stackoverflow.com/questions/978061/http-get-with-request-body](https://stackoverflow.com/questions/978061/http-get-with-request-body)

