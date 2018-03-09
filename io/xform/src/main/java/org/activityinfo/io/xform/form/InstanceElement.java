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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
