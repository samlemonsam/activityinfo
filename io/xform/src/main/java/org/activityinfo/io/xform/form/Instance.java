package org.activityinfo.io.xform.form;

import javax.xml.bind.annotation.XmlAnyElement;

public class Instance {

    private InstanceElement root;

    public Instance() {
    }

    public Instance(InstanceElement root) {
       this.root = root;
    }

    @XmlAnyElement
    public InstanceElement getRoot() {
        return root;
    }

    public void setRoot(InstanceElement root) {
        this.root = root;
    }
}
