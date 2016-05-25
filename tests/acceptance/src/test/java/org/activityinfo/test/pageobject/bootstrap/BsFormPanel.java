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

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.Sleep;
import org.activityinfo.test.driver.ControlType;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.FluentElements;
import org.activityinfo.test.pageobject.api.XPathBuilder;
import org.activityinfo.test.pageobject.web.components.Form;
import org.activityinfo.test.ui.ImagePathProvider;
import org.joda.time.LocalDate;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;

/**
 * @author yuriyz on 05/12/2015.
 */
public class BsFormPanel extends Form {

    private final FluentElement form;
    private BsField current;
    private int index;

    public BsFormPanel(FluentElement form) {
        this(form, 0);
    }

    public BsFormPanel(FluentElement form, int index) {
        this.form = form;
        this.index = index;
    }

    @Override
    public BsField findFieldByLabel(String labelText) {
        Optional<FluentElement> element = form.find().label(withText(labelText)).ancestor().div(withClass("form-group")).firstIfPresent();
        if (element.isPresent()) {
            List<FluentElement> list = form.find().label(withText(labelText)).ancestor().div(withClass("form-group")).asList().list();
            return new BsField(list.get(index));
        }

        element = form.find().label(withText(labelText)).ancestor().span(withClass("radio")).firstIfPresent();
        if (element.isPresent()) {
            return new BsField(element.get());
        }

        throw new AssertionError(String.format("The form panel has no field with label %s", labelText));
    }

    /**
     * Useful for repeating subforms where we may have many fields with the same label.
     *
     * @param labelText label text
     * @return all fields by label
     */
    public List<BsField> findFieldsByLabel(String labelText) {
        List<BsField> result = Lists.newArrayList();

        XPathBuilder div = form.find().label(withText(labelText)).ancestor().div(withClass("form-group"));
        if (div.firstIfPresent().isPresent()) {
            for (FluentElement element : div.waitForList().list()) {
                result.add(new BsField(element));
            }
            return result;
        }

        XPathBuilder label = form.find().label(withText(labelText)).ancestor().span(withClass("radio"));
        if (label.firstIfPresent().isPresent()) {
            for (FluentElement element : label.waitForList().list()) {
                result.add(new BsField(element));
            }
        }
        return result;
    }

    @Override
    public boolean moveToNext() {
        Optional<FluentElement> first;

        if (current == null) {
            first = form.find().div(withClass("form-group")).firstIfPresent();
        } else {
            first = current.element.find().followingSibling().div(withClass("form-group")).firstIfPresent();
        }
        if (first.isPresent()) {
            current = new BsField(first.get());
            return true;
        } else {
            current = null;
            return false;
        }
    }

    public FluentElement getForm() {
        return form;
    }

    @Override
    public FormItem current() {
        return current;
    }

    public static class BsField implements Form.FormItem {

        private final FluentElement element;

        public BsField(FluentElement element) {
            this.element = element;
        }

        @Override
        public String getLabel() {
            return element.findElement(By.tagName("label")).text();
        }

        @Override
        public String getPlaceholder() {
            return input().attribute("placeholder");
        }

        @Override
        public boolean isDropDown() {
            return isGwtDropDown() || isDropDownWithSuggestBox();
        }

        private boolean isGwtDropDown() {
            Optional<FluentElement> select = element.find().select().firstIfPresent();
            if (select.isPresent()) {
                String classAttr = select.get().element().getAttribute("class");
                String selectWithSuggestBoxClass = "chzn-done";
                if (!classAttr.contains(selectWithSuggestBoxClass)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isDropDownWithSuggestBox() {
            return element.exists(By.tagName("a"));
        }

        @Override
        public boolean isSuggestBox() {
            return getPlaceholder().equals(I18N.CONSTANTS.suggestBoxPlaceholder());
        }

        public boolean isCheckBox() {
            return element.exists(By.className("checkbox"));
        }

        public void fill(String value, String controlType) {
            if ("radio".equalsIgnoreCase(controlType) || "dropdown".equalsIgnoreCase(controlType)) {
                select(value);
            } else if ("date".equalsIgnoreCase(controlType)) {
                fill(org.joda.time.LocalDate.parse(value));
            } else {
                fill(value);
            }
        }

        @Override
        public void fill(String value) {
            FluentElement input = input();

            if (input.element().getAttribute("type").equals("file")) { // file upload
                input.sendKeys(ImagePathProvider.path(value));
                Sleep.sleepSeconds(10); // make sure file is uploaded
            } else {
                input.element().clear();
                input.sendKeys(value);
            }
        }

        private FluentElement input() {
            Optional<FluentElement> input = element.find().input().firstIfPresent();
            if (input.isPresent()) {
                return input.get();
            }
            Optional<FluentElement> textArea = element.find().textArea().firstIfPresent();
            if (textArea.isPresent()) {
                return textArea.get();
            }

            throw new AssertionError("Failed to locate input/textarea element.");
        }

        @Override
        public void fill(LocalDate date) {
            fill(date.toString("M/d/YY") + "\n");
        }

        private FluentElements items() {
            final FluentElements items;
            if (isDropDown()) {
                if (isGwtDropDown()) {
                    element.findElement(By.tagName("select")).click();

                    FluentElement list = this.element.waitFor(By.tagName("select"));
                    items = list.findElements(By.tagName("option"));
                } else if (isDropDownWithSuggestBox()) {
                    element.findElement(By.tagName("a")).click();

                    FluentElement list = this.element.waitFor(By.tagName("ul"));
                    items = list.findElements(By.tagName("li"));
                } else {
                    throw new RuntimeException("Failed to identify type of dropdown control.");
                }
            } else {
                items = element.findElements(By.tagName("label"));
            }
            return items;
        }

        public List<String> itemLabels() {
            List<String> itemLabels = Lists.newArrayList();

            for (FluentElement element : items()) {
                String text = element.text();
                itemLabels.add(text);
            }
            return itemLabels;
        }


        @Override
        public void select(String itemLabel) {
            if (isGwtDropDown()) {
                Select select = new Select(element.find().select().first().element());
                select.selectByVisibleText(itemLabel);
                return;
            }

            final FluentElements items = items();

            List<String> itemLabels = Lists.newArrayList();
            for (FluentElement element : items) {
                String text = element.text();
                if (Strings.isNullOrEmpty(text)) {
                    text = Strings.nullToEmpty(element.element().getAttribute("text"));
                }
                itemLabels.add(text);
                if (text.equalsIgnoreCase(itemLabel)) {
                    element.click();
                    return;
                }
            }

            // Report nice error message
            throw new AssertionError(String.format("Could not select '%s' from combo box '%s'. Options:\n%s",
                    itemLabel,
                    getLabel(),
                    Joiner.on("\n").join(itemLabels)));
        }

        @Override
        public boolean isEnabled() {
            if (isCheckBox()) {
                return !element.exists(By.className("checkbox-disabled"));
            }
            if (isRadio()) {
                return !element.exists(By.className("radio-disabled"));
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public FluentElement getElement() {
            return element;
        }

        @Override
        public boolean isValid() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> availableItems() {
            final FluentElements items = items();

            List<String> itemLabels = Lists.newArrayList();
            boolean skipFirst = !isDropDown(); // if not drop down we gather all labels including widget lable, so we want skip it here
            for (FluentElement element : items) {
                String text = element.text();
                if (Strings.isNullOrEmpty(text)) {
                    text = Strings.nullToEmpty(element.element().getAttribute("text"));
                }
                if (skipFirst) {
                    skipFirst = false;
                    continue;
                }
                itemLabels.add(text);
            }
            return itemLabels;
        }

        private FluentElement radioElement(String label) {
            return element.find().label(withText(label)).precedingSibling().input().first();
        }

        public boolean isRadio() {
            return element.exists(By.className("radio"));
        }

        public boolean isRadioSelected(String label) {
            FluentElement radio = radioElement(label);
            Preconditions.checkState(radio.element().getAttribute("type").equals("radio"), "Element is not radio element");
            return radio.element().isSelected();
        }

        public ControlType getControlType() {
            if (isRadio()) {
                return ControlType.RADIO_BUTTONS;
            } else if (isCheckBox()) {
                return ControlType.CHECK_BOXES;
            } else if (isDropDown()) {
                return ControlType.DROP_DOWN;
            } else if (isSuggestBox()) {
                return ControlType.SUGGEST_BOX;
            }
            return null;
        }

        public boolean isEmpty() {
            if (isRadio()) {
                for (String item : availableItems()) {
                    if (isRadioSelected(item)) {
                        return false;
                    }
                }
                return true;
            }
            throw new UnsupportedOperationException();
        }

        public boolean isBlobImageLoaded() {
            return isBlobImageLoaded("googleusercontent.com");
        }

        public boolean isBlobImageLoaded(String expectedSrcContains) {
            FluentElement img = element.find().img().first();
            String src = img.attribute("src");
            return !Strings.isNullOrEmpty(src) && src.contains(expectedSrcContains);
        }

        public List<String> getBlobLinks() {
            List<String> result = Lists.newArrayList();
            for (FluentElement elem : element.find().a().waitForList().list()) {
                String href = elem.attribute("href");
                if (!Strings.isNullOrEmpty(href) && !href.endsWith("#")) {
                    result.add(href);
                }
            }
            return result;
        }

        public String getFirstBlobLink() {
            return getBlobLinks().get(0);
        }

        public String getValue(ControlType controlType) {
            switch (controlType) {
                case TEXT:
                case QUANTITY:
                case DATE:
                    return input().attribute("value");
            }
            throw new RuntimeException("Unsupported control type: " + controlType);
        }
    }
}
