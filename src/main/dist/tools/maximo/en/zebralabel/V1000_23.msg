<?xml version="1.0" encoding="UTF-8"?>
<!--suppress CheckTagEmptyBody -->
<messages cleanup="false" >
    <Message id="NAVZL0001E" group="naviam" key="invalidHost" prefix="1">
        <Display method="MSGBOX">
            <Option>MSG_BTNOK</Option>
        </Display>
        <MsgText>{0} is not a valid host name or IP address.</MsgText>
        <Explanation></Explanation>
        <AdminResponse></AdminResponse>
        <OperatorResponse></OperatorResponse>
    </Message>
    <Message id="NAVZL0002E" group="naviam" key="invalidPort" prefix="1">
        <Display method="MSGBOX">
            <Option>MSG_BTNOK</Option>
        </Display>
        <MsgText>{0} is not a valid port number, port numbers must be between 0 and 65535.</MsgText>
        <Explanation></Explanation>
        <AdminResponse></AdminResponse>
        <OperatorResponse></OperatorResponse>
    </Message>
    <Message id="NAVZL0003E" group="naviam" key="invalidStoreroom" prefix="1">
        <Display method="MSGBOX">
            <Option>MSG_BTNOK</Option>
        </Display>
        <MsgText>{1} is not a valid storeroom.</MsgText>
        <Explanation></Explanation>
        <AdminResponse></AdminResponse>
        <OperatorResponse></OperatorResponse>
    </Message>
    <Message id="NAVZL0004I" group="naviam" key="changeDefaultLabel" prefix="1">
        <Display method="MSGBOX">
            <Option>MSG_BTNYES</Option>
            <Option>MSG_BTNNO</Option>
            <Option>MSG_BTNCANCEL</Option>
        </Display>
        <MsgText>Another label is already declared as the default for the {0} media, do you want to change the default label?</MsgText>
        <Explanation></Explanation>
        <AdminResponse></AdminResponse>
        <OperatorResponse></OperatorResponse>
    </Message>
    <Message id="NAVZL0005I" group="naviam" key="labelPrinted" prefix="1">
        <Display method="STATUS">
            <Option>MSG_BTNOK</Option>
        </Display>
        <MsgText>Label for {0} {1} printed.</MsgText>
        <Explanation></Explanation>
        <AdminResponse></AdminResponse>
        <OperatorResponse></OperatorResponse>
    </Message>
    <Message id="NAVZL0005E" group="naviam" key="printError" prefix="1">
        <Display method="MSGBOX">
            <Option>MSG_BTNOK</Option>
        </Display>
        <MsgText>Error Printing Label:\n{0}.</MsgText>
        <Explanation></Explanation>
        <AdminResponse></AdminResponse>
        <OperatorResponse></OperatorResponse>
    </Message>
    <Message id="NAVZL0006E" group="naviam" key="invalidZPL" prefix="1">
        <Display method="MSGBOX">
            <Option>MSG_BTNOK</Option>
        </Display>
        <MsgText>The entered ZPL is invalid, it must start with ^XA and end with ^XZ.</MsgText>
        <Explanation></Explanation>
        <AdminResponse></AdminResponse>
        <OperatorResponse></OperatorResponse>
    </Message>
    <Message id="NAVZL0007E" group="naviam" key="countLessThanOne" prefix="1">
        <Display method="MSGBOX">
            <Option>MSG_BTNOK</Option>
        </Display>
        <MsgText>The label count cannot be less than one.</MsgText>
        <Explanation></Explanation>
        <AdminResponse></AdminResponse>
        <OperatorResponse></OperatorResponse>
    </Message>
    <Message id="NAVZL0008E" group="naviam" key="countGreaterThanMax" prefix="1">
        <Display method="MSGBOX">
            <Option>MSG_BTNOK</Option>
        </Display>
        <MsgText>The label count cannot be greater than {1}.</MsgText>
        <Explanation></Explanation>
        <AdminResponse></AdminResponse>
        <OperatorResponse></OperatorResponse>
    </Message>
    <Message id="NAVZL0009E" group="naviam" key="noToStoreLoc" prefix="1">
        <Display method="MSGBOX">
            <Option>MSG_BTNOK</Option>
        </Display>
        <MsgText>Labels can only be printed for items received to a storeroom.</MsgText>
        <Explanation></Explanation>
        <AdminResponse></AdminResponse>
        <OperatorResponse></OperatorResponse>
    </Message>
    <Message id="NAVZL0010I" group="naviam" key="changeDefaultPrinter" prefix="1">
        <Display method="MSGBOX">
            <Option>MSG_BTNYES</Option>
            <Option>MSG_BTNNO</Option>
            <Option>MSG_BTNCANCEL</Option>
        </Display>
        <MsgText>Another printer is already declared as the default for the {0} store, do you want to change the default printer?</MsgText>
        <Explanation></Explanation>
        <AdminResponse></AdminResponse>
        <OperatorResponse></OperatorResponse>
    </Message>
    <Message id="NAVZL0011E" group="naviam" key="unknownPrintError" prefix="1">
        <Display method="MSGBOX">
            <Option>MSG_BTNOK</Option>
        </Display>
        <MsgText>An unexpected printing error has occurred: {0}.</MsgText>
        <Explanation></Explanation>
        <AdminResponse></AdminResponse>
        <OperatorResponse></OperatorResponse>
    </Message>
    <Message id="NAVZL0012E" group="naviam" key="noprinter" prefix="1">
        <Display method="MSGBOX">
            <Option>MSG_BTNOK</Option>
        </Display>
        <MsgText>A printer must be selected to print a label.</MsgText>
        <Explanation></Explanation>
        <AdminResponse></AdminResponse>
        <OperatorResponse></OperatorResponse>
    </Message>
    <Message id="NAVZL0013E" group="naviam" key="invalidPrinter" prefix="1">
        <Display method="MSGBOX">
            <Option>MSG_BTNOK</Option>
        </Display>
        <MsgText>Printer {0} is not a valid printer.</MsgText>
        <Explanation></Explanation>
        <AdminResponse></AdminResponse>
        <OperatorResponse></OperatorResponse>
    </Message>
    <Message id="NAVZL0014E" group="naviam" key="nolabel" prefix="1">
        <Display method="MSGBOX">
            <Option>MSG_BTNOK</Option>
        </Display>
        <MsgText>A label must be selected to print a label.</MsgText>
        <Explanation></Explanation>
        <AdminResponse></AdminResponse>
        <OperatorResponse></OperatorResponse>
    </Message>
    <Message id="NAVZL0015E" group="naviam" key="invalidLabel" prefix="1">
        <Display method="MSGBOX">
            <Option>MSG_BTNOK</Option>
        </Display>
        <MsgText>Label {0} is not a valid label.</MsgText>
        <Explanation></Explanation>
        <AdminResponse></AdminResponse>
        <OperatorResponse></OperatorResponse>
    </Message>    
</messages>