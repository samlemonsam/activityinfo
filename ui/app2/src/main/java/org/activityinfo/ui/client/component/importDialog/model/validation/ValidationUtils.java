package org.activityinfo.ui.client.component.importDialog.model.validation;
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
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import java.util.List;

/**
 * @author yuriyz on 3/11/14.
 */
public class ValidationUtils {

    private ValidationUtils() {
    }

    public static void showMessage(String message, DivElement divContainer){
        showMessages(Lists.newArrayList(message), divContainer);
    }

    public static void addMessage(String message, DivElement divContainer){
        final SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
        safeHtmlBuilder.appendHtmlConstant(divContainer.getInnerHTML()).
                appendHtmlConstant("<br/>").
                append(SafeHtmlUtils.fromString(message));

        divContainer.setInnerHTML(safeHtmlBuilder.toSafeHtml().asString());
    }

    public static void showMessages(List<String> messages, DivElement divContainer) {
        final SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
        for (String message : messages) {
            safeHtmlBuilder.append(SafeHtmlUtils.fromString(message))
                    .appendHtmlConstant("<br/>");
        }
        divContainer.setInnerHTML(safeHtmlBuilder.toSafeHtml().asString());
    }

    public static String format(String controlName, String validationMessage) {
        return controlName + " - " + validationMessage;
    }
}
