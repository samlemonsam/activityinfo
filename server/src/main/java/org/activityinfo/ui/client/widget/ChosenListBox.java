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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.watopi.chosen.client.ChosenOptions;

/**
 * @author yuriyz on 12/30/2015.
 */
public class ChosenListBox extends com.watopi.chosen.client.gwt.ChosenListBox {

    public static final int DEFAULT_WIDTH = 250;

    public ChosenListBox() {
        this(false);
    }

    public ChosenListBox(ChosenOptions options) {
        this(false, options);
    }

    public ChosenListBox(boolean isMultipleSelect) {
        this(isMultipleSelect, new ChosenOptions());
    }

    public ChosenListBox(boolean isMultipleSelect, ChosenOptions options) {
        super(isMultipleSelect, options);
        forceRedrawLater();
    }

    protected ChosenListBox(Element element) {
        super(element);
        forceRedrawLater();
    }

    private void forceRedrawLater() {
        forceRedrawLater(true);
    }

    // Workaround for ugly fix in com.watopi.chosen.client.ChosenImpl.setup(), line 1207 :
    // Temporary fix. IIf the select element is inside a hidden container
    // GQuery cannot get the size of the select element.
    private void forceRedrawLater(final boolean allowRecursiveCall) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                forceRedraw();
                makeItBootstrapish(allowRecursiveCall);
            }
        });
    }

    private void makeItBootstrapish(boolean allowRecursiveCall) {
        NodeList<Element> linkList = getElement().getParentElement().getElementsByTagName("a");
        Element link = linkList.getItem(0);
        link.setClassName("form-control");

        makeWidthIsNotCorrupted(link, allowRecursiveCall);
    }

    // Workaround for ugly fix in com.watopi.chosen.client.ChosenImpl.setup(), line 1207 :
    // Temporary fix. IIf the select element is inside a hidden container
    // GQuery cannot get the size of the select element.
    private void makeWidthIsNotCorrupted(Element link, boolean allowRecursiveCall) {
        if (link.getOffsetWidth() > 1000) {
            if (allowRecursiveCall) {
                forceRedrawLater(false);
            } else { // fallback,  in 99% should not happen
                link.getParentElement().getStyle().setWidth(DEFAULT_WIDTH + 2, Style.Unit.PX);
                link.getStyle().setWidth(DEFAULT_WIDTH, Style.Unit.PX);
                link.getNextSiblingElement().getStyle().setWidth(DEFAULT_WIDTH, Style.Unit.PX);
                Element input = link.getNextSiblingElement().getElementsByTagName("input").getItem(0);
                input.getStyle().setWidth(DEFAULT_WIDTH - 15, Style.Unit.PX);
            }
        }
    }
}

