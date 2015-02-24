package org.activityinfo.ui.client.component.formdesigner;
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

import com.bedatadriven.rebar.style.client.Source;
import com.bedatadriven.rebar.style.client.Stylesheet;
import com.google.gwt.core.client.GWT;

/**
 * @author yuriyz on 07/04/2014.
 */
@Source("FormDesigner.less")
public interface FormDesignerStyles extends Stylesheet{

    public static final FormDesignerStyles INSTANCE = GWT.create(FormDesignerStyles.class);

    @ClassName("widget-container")
    String widgetContainer();

    @ClassName("widget-container-selected")
    String widgetContainerSelected();

    @ClassName("spacer")
    String spacer();

    @ClassName("spacer-normal")
    String spacerNormal();

    @ClassName("spacer-forbidden")
    String spacerForbidden();

    @ClassName("header-container")
    String headerContainer();

    @ClassName("main-panel")
    String mainPanel();

    @ClassName("section-widget-container")
    String sectionWidgetContainer();

    @ClassName("section-widget-container-selected")
    String sectionWidgetContainerSelected();

    @ClassName("section-label")
    String sectionLabel();


}
