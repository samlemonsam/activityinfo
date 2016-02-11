package org.activityinfo.test.pageobject.bootstrap;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Lists;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.openqa.selenium.By;

import java.util.List;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;

/**
 * @author yuriyz on 02/11/2016.
 */
public class SubformContainer {

    private final BsFormPanel form;
    private final FluentElement headerDiv;

    public SubformContainer(BsFormPanel form, String subformName) {
        this.form = form;
        this.headerDiv = form.getForm().find().h4(withText(subformName)).
                ancestor().div().first();
    }

    private FluentElement getAddButton() {
        return headerDiv.find().followingSibling().button(withText(I18N.CONSTANTS.addAnother())).first();
    }

    public SubformContainer addAnother() {
        getAddButton().clickWhenReady();
        return this;
    }

    public List<SubformPanel> getPanels() {
        List<SubformPanel> panels = Lists.newArrayList();
        List<FluentElement> elements = headerDiv.findElements(By.xpath("following-sibling::*")).list();
        for (FluentElement element : elements) {
            if (element.getTagName().equalsIgnoreCase("div") && element.attribute("class").contains("subformPanel")) {
                panels.add(new SubformPanel(element));
            } else {
                break;
            }
        }
        return panels;
    }

    public int getRepeatingPanelsCount() {
        return getPanels().size();
    }

    public List<BsFormPanel.BsField> findFieldsByLabel(String labelText) {
        return form.findFieldsByLabel(labelText);
    }
}
