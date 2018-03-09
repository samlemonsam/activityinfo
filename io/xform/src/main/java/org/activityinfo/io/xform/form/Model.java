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
