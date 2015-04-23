package org.activityinfo.io.xform.form;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlJavaTypeAdapter(InstanceElementAdapter.class)
public class InstanceElement {
    private String id;
    private String name;
    private String value;
    private List<InstanceElement> children;

    public InstanceElement(String name) {
        this.name = name;
        this.children = Lists.newArrayList();
    }

    public InstanceElement(String name, InstanceElement... children) {
        this.name = name;
        this.children = Lists.newArrayList(children);
    }

    public InstanceElement(String name, String value) {
        this.name = name;
        this.value = value;
        this.children = Lists.newArrayList();
    }


    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = Strings.emptyToNull(value);
    }

    public List<InstanceElement> getChildren() {
        return children;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean hasChildren() {
        return this.children.size() > 0;
    }

    public void addChild(InstanceElement instanceElement) {
        children.add(instanceElement);
    }
}
