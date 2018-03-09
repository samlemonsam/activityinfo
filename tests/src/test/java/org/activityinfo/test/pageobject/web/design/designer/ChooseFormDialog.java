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
package org.activityinfo.test.pageobject.web.design.designer;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.driver.AliasTable;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.XPathBuilder;
import org.activityinfo.test.pageobject.bootstrap.BsModal;

import java.util.List;
import java.util.Set;

/**
 * Created by yuriyz on 4/29/2016.
 */
public class ChooseFormDialog {

    private final BsModal modal;

    public ChooseFormDialog(BsModal modal) {
        this.modal = modal;
    }

    public BsModal getModal() {
        return modal;
    }

    private SetMultimap<String, FluentElement> nodes() { // parsing manually, for some reason /div containsText(nodeName) does not work
        SetMultimap<String, FluentElement> nodes = HashMultimap.create();
        FluentElement formElement = modal.form().getForm();
        for (FluentElement node : formElement.find().span(XPathBuilder.withClass("icon")).ancestor().div().asList().list()) {
            String text = node.text();
            if (!Strings.isNullOrEmpty(text)) {
                nodes.put(text, node);
            }

        }
        return nodes;
    }

    public ChooseFormDialog set(List<String> path, AliasTable alias) {

        for (int i = 0; i < path.size(); i++) {
            String nodeName = path.get(i);
            String homeNode = path.get(0);
            if (i > 0 && !homeNode.equals(I18N.CONSTANTS.geography()) &&
                    !nodeName.equals(I18N.CONSTANTS.partners()) &&
                    !nodeName.equals(I18N.CONSTANTS.projects())) {
                nodeName = alias.createAlias(nodeName);
            }

            Set<FluentElement> fluentElements = nodes().get(nodeName);
            if (fluentElements != null) {
                fluentElements.iterator().next().clickWhenReady(); // click first
            } else {
                throw new RuntimeException("ChooseFormDialog : Unable to find node with name: " + nodeName);
            }
        }
        return save();
    }

    public ChooseFormDialog save() {
        modal.accept();
        return this;
    }
}
