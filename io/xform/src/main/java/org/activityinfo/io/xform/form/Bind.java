/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
