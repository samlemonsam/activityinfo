package org.activityinfo.io.xform.form;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public abstract class SelectElement extends BodyElement {

    private List<Item> items = Lists.newArrayList();

    @XmlElement(name = "item")
    public List<Item> getItems() {
        return items;
    }
}
