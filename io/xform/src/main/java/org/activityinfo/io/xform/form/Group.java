package org.activityinfo.io.xform.form;

import com.google.common.collect.Lists;
import org.activityinfo.io.xform.Namespaces;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import java.util.List;

public class Group extends BodyElement {

    @XmlElements({
            @XmlElement(name = "group", namespace = Namespaces.XFORM, type = Group.class),
            @XmlElement(name = "select1", namespace = Namespaces.XFORM, type = Select1.class),
            @XmlElement(name = "select", namespace = Namespaces.XFORM, type = Select.class),
            @XmlElement(name = "input", namespace = Namespaces.XFORM, type = Input.class),
            @XmlElement(name = "upload", namespace = Namespaces.XFORM, type = Upload.class)
    })
    private List<BodyElement> elements = Lists.newArrayList();

    public List<BodyElement> getElements() {
        return elements;
    }
}
