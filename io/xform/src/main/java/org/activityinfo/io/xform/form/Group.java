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
