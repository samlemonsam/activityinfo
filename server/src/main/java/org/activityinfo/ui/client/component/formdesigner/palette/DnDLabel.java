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

import com.google.gwt.user.client.ui.Label;
import org.activityinfo.ui.client.component.formdesigner.FormDesignerConstants;
import org.activityinfo.ui.client.component.formdesigner.drag.IsDraggable;

/**
 * @author yuriyz on 12/05/2014.
 */
public class DnDLabel extends Label implements IsDraggable {

    private final Template template;

    public DnDLabel(Template template) {
        super(template.getLabel());
        this.template = template;
        setStyleName(FormDesignerConstants.DEFAULT_STYLE_NAME);
        setWidth(FormDesignerConstants.SOURCE_CONTROL_WIDTH_PX + "px");
    }

    public Template getTemplate() {
        return template;
    }
}
