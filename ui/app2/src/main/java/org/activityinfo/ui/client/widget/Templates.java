package org.activityinfo.ui.client.widget;
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
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * @author yuriyz on 4/16/14.
 */
public class Templates {

    public interface WarningMessageTemplate extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<p class='text-warning'>{0}</p>")
        SafeHtml html(String message);
    }

    public static interface OkButtonTemplate extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<img src='ActivityInfo/gxt231/images/default/grid/loading.gif'/>{0}")
        SafeHtml html(String buttonText);
    }

    public static final WarningMessageTemplate WARNING_MESSAGE_TEMPLATE = GWT.create(WarningMessageTemplate.class);
    public static final OkButtonTemplate OK_BTN_TEMPLATE = GWT.create(OkButtonTemplate.class);
}
