package org.activityinfo.ui.client.component.formdesigner.skip;
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

/**
 * @author yuriyz on 7/24/14.
 */
public class SkipRow extends Composite {

    private static OurUiBinder uiBinder = GWT.create(OurUiBinder.class);

    interface OurUiBinder extends UiBinder<Widget, SkipRow> {
    }

    @UiField
    Button removeButton;
    @UiField
    Button addButton;
    @UiField
    HTMLPanel valueContainer;
    @UiField
    ListBox function;
    @UiField
    ListBox formfield;
    @UiField
    HTMLPanel skipGroup;
    @UiField
    ListBox joinFunction;

    public SkipRow() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public Button getRemoveButton() {
        return removeButton;
    }

    public Button getAddButton() {
        return addButton;
    }

    public HTMLPanel getValueContainer() {
        return valueContainer;
    }

    public ListBox getFunction() {
        return function;
    }

    public ListBox getFormfield() {
        return formfield;
    }

    public HTMLPanel getSkipGroup() {
        return skipGroup;
    }

    public ListBox getJoinFunction() {
        return joinFunction;
    }
}
