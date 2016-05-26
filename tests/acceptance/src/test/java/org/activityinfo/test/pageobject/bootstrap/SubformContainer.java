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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.Sleep;
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
    private SubformPanel keyedPanel;

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

    private SubformPanel keyedPanel() {
        if (keyedPanel == null) {
            keyedPanel = getPanels().get(0); // for keyed subforms we have only one panel
        }
        return keyedPanel;
    }

    public SubformContainer selectKey(String keyLabel) {
        boolean clickedOneTimeForward = false;
        for (int i = 0; i < 10; i++) {
            Optional<FluentElement> key = navButtonByLabel(keyLabel);
            Sleep.sleepSeconds(1);

            if (key.isPresent()) {
                key.get().clickWhenReady();
                return this;
            } else {
                if (!clickedOneTimeForward) {
                    clickNextFull();
                    clickedOneTimeForward = true;
                } else {
                    clickPreviousFull();
                }
            }
        }

        throw new RuntimeException("Failed to find tab with label: " + keyLabel);
    }

    private Optional<FluentElement> navButtonByLabel(String label) {
        List<FluentElement> links = keyedPanel().getElement().find().a().waitForList().list();
        for (FluentElement element : links) {
            if (label.equals(element.text())) {
                return Optional.of(element);
            }
        }
        return Optional.absent();
    }

    public SubformContainer clickPreviousFull() {
        navButtonByLabel("«").get().clickWhenReady();
        return this;
    }

    public SubformContainer clickPrevious() {
        navButtonByLabel("<").get().clickWhenReady();
        return this;
    }

    public SubformContainer clickNext() {
        navButtonByLabel(">").get().clickWhenReady();
        return this;
    }

    public SubformContainer clickNextFull() {
        navButtonByLabel("»").get().clickWhenReady();
        return this;
    }

}
