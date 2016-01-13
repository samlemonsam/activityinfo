package org.activityinfo.ui.client.component.formdesigner.palette;
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

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.type.attachment.AttachmentType;

/**
 * @author yuriyz on 11/13/2015.
 */
public class AttachmentFieldTemplate implements FieldTemplate {

    private final String label;
    private final AttachmentType.Kind kind;

    public AttachmentFieldTemplate(AttachmentType.Kind kind, String label) {
        this.label = label;
        this.kind = kind;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public FormField createField() {
        AttachmentType type = (AttachmentType) AttachmentType.TYPE_CLASS.createType();
        type.setKind(kind);

        FormField formField = new FormField(CuidAdapter.indicatorField(new KeyGenerator().generateInt()));
        formField.setType(type);
        formField.setLabel(label);
        return formField;
    }
}

