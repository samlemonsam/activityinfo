package org.activityinfo.io.xform.form;

import javax.xml.bind.annotation.XmlElement;

public class Item {

    private String label;
    private String value;

    @XmlElement
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @XmlElement
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
