package org.activityinfo.ui.client.component.formdesigner.properties;
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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.type.subform.SubFormKind;
import org.activityinfo.model.type.subform.SubFormKindRegistry;
import org.activityinfo.ui.client.widget.TextBox;
import org.activityinfo.ui.client.widget.form.FormGroup;

/**
 * @author yuriyz on 01/15/2015.
 */
public class ContainerPropertiesPanel extends Composite {

    private static OurUiBinder uiBinder = GWT.create(OurUiBinder.class);

    interface OurUiBinder extends UiBinder<Widget, ContainerPropertiesPanel> {
    }

    @UiField
    TextBox label;
    @UiField
    FormGroup labelGroup;
    @UiField
    ListBox subformKind;
    @UiField
    FormGroup subformKindGroup;

    public ContainerPropertiesPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        initSubformCombobox();
    }

    private void initSubformCombobox() {
        for (SubFormKind kind : SubFormKindRegistry.get().getKinds()) {
            subformKind.addItem(kind.getDefinition().getLabel(), kind.getDefinition().getId().asString());
        }
    }

    public TextBox getLabel() {
        return label;
    }

    public FormGroup getLabelGroup() {
        return labelGroup;
    }

    public ListBox getSubformKind() {
        return subformKind;
    }

    public FormGroup getSubformKindGroup() {
        return subformKindGroup;
    }
}
