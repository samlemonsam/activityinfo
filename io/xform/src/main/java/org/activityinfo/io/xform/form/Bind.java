package org.activityinfo.io.xform.form;

import javax.xml.bind.annotation.XmlAttribute;

public class Bind {
    private String nodeSet;

    private BindingType type;

    private String readonly;

    private String calculate;

    private String required;

    private String relevant;

    private String constraint;

    private String constraintMessage;

    private String preload;

    private String preloadParams;

    public Bind() {

    }

    public Bind(BindingType modelBindType) {
        type = modelBindType;
    }

    @SuppressWarnings("SpellCheckingInspection")
    @XmlAttribute(name = "nodeset")
    public String getNodeSet() {
        return nodeSet;
    }

    public void setNodeSet(String nodeSet) {
        this.nodeSet = nodeSet;
    }

    @XmlAttribute
    public BindingType getType() {
        return type;
    }

    public void setType(BindingType type) {
        this.type = type;
    }

    @XmlAttribute
    public String getReadonly() {
        return readonly;
    }

    public void setReadonly(String readonly) {
        this.readonly = readonly;
    }

    @XmlAttribute
    public String getCalculate() {
        return calculate;
    }

    public void setCalculate(String calculate) {
        this.calculate = calculate;
    }

    @XmlAttribute
    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    @XmlAttribute
    public String getRelevant() {
        return relevant;
    }

    public void setRelevant(String relevant) {
        this.relevant = relevant;
    }

    @XmlAttribute
    public String getConstraint() {
        return constraint;
    }

    public void setConstraint(String constraint) {
        this.constraint = constraint;
    }

    @XmlAttribute(namespace = "http://openrosa.org/javarosa", name = "constraintMsg")
    public String getConstraintMessage() {
        return constraintMessage;
    }

    public void setConstraintMessage(String constraintMessage) {
        this.constraintMessage = constraintMessage;
    }

    @XmlAttribute(namespace = "http://openrosa.org/javarosa")
    public String getPreload() {
        return preload;
    }

    public void setPreload(String preload) {
        this.preload = preload;
    }

    @XmlAttribute(namespace = "http://openrosa.org/javarosa")
    public String getPreloadParams() {
        return preloadParams;
    }

    public void setPreloadParams(String preloadParams) {
        this.preloadParams = preloadParams;
    }
}
