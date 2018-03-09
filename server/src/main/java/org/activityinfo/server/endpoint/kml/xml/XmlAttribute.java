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
package org.activityinfo.server.endpoint.kml.xml;

public class XmlAttribute {

    private String namespace = null;
    private String name;
    private StringBuilder value = new StringBuilder();

    public XmlAttribute(String namespace, String name) {
        this.name = name;
        this.namespace = namespace;
    }

    public XmlAttribute(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value.toString();
    }

    protected void clear() {
        if (value.length() != 0) {
            value = new StringBuilder();
        }
    }

    public void setValue(String value) {
        clear();
        this.value.append(value);
    }

    public void setValue(int value) {
        clear();
        this.value.append(value);
    }

    public XmlAttribute append(String value) {
        this.value.append(value);
        return this;
    }

    public XmlAttribute append(String value, char delimeter) {
        if (this.value.length() != 0) {
            this.value.append(delimeter);
        }
        this.value.append(value);
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

}
