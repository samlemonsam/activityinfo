package org.activityinfo.io.xform.form;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlType(propOrder = {"itext", "instance", "bindings"})
public class Model {

    private IText itext = new IText();
    private Instance instance = new Instance();
    private List<Bind> bindings = new ArrayList<>();


    @XmlElement(name = "itext")
    public IText getItext() {
        return itext;
    }

    public void setItext(IText itext) {
        this.itext = itext;
    }

    @XmlElement
    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    @XmlElement(name = "bind")
    public List<Bind> getBindings() {
        return bindings;
    }

    public void addBinding(Bind bind) {
        this.bindings.add(bind);
    }

}
