package org.activityinfo.io.xform.form;

import javax.xml.bind.annotation.XmlEnumValue;

public enum BindingType {

    @XmlEnumValue("string")
    STRING,

    @XmlEnumValue("dateTime")
    DATETIME,

    @XmlEnumValue("select1")
    SELECT1,

    @XmlEnumValue("date")
    DATE,

    @XmlEnumValue("int")
    INT,

    @XmlEnumValue("decimal")
    DECIMAL,

    @XmlEnumValue("select")
    SELECT,

    @XmlEnumValue("geopoint")
    GEOPOINT,

    @XmlEnumValue("binary")
    BINARY,

    @XmlEnumValue("boolean")
    BOOLEAN,

    @XmlEnumValue("barcode")
    BARCODE


}
