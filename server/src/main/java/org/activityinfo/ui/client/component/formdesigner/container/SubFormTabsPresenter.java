package org.activityinfo.ui.client.component.formdesigner.container;
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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormInstanceLabeler;
import org.activityinfo.model.type.subform.SubformConstants;

import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 02/04/2015.
 */
public class SubFormTabsPresenter {

    private final SubFormTabs view;

    private int tabCount = SubformConstants.DEFAULT_TAB_COUNT;

    private final Map<String, FormInstance> formInstances = Maps.newHashMap();
    private final List<HandlerRegistration> clickHandlers = Lists.newArrayList();

    public SubFormTabsPresenter(SubFormTabs view) {
        this.view = view;
    }

    public void set(List<FormInstance> instances) {
        formInstances.clear();
        clickHandlers.clear();

        String safeHtml = perviousButtons();
        for (FormInstance instance : instances) {
            formInstances.put(instance.getId().asString(), instance);

            String escapedLabel = SafeHtmlUtils.fromString(FormInstanceLabeler.getLabel(instance)).asString();
            safeHtml = safeHtml + "<li><a href='javascript:' id='" + instance.getId().asString() + "'>" + escapedLabel + "</a></li>";
        }

        safeHtml = safeHtml + nextButtons();

        view.getSubformTabsUl().removeAllChildren();
        view.getSubformTabsUl().setInnerSafeHtml(SafeHtmlUtils.fromTrustedString(safeHtml));

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                bindClickHandlers();
            }
        });
    }

    private void bindClickHandlers() {

        // predefined buttons
        addClickHandlerToElementById("_fullprevious");
        addClickHandlerToElementById("_previous");
        addClickHandlerToElementById("_next");
        addClickHandlerToElementById("_fullnext");

        // tabs
        for (String id : formInstances.keySet()) {
            addClickHandlerToElementById(id);
        }
    }

    private void addClickHandlerToElementById(final String elementId) {
        com.google.gwt.dom.client.Element elementById = Document.get().getElementById(elementId);
        Event.sinkEvents(elementById, Event.ONCLICK);
        Event.setEventListener(elementById, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if (Event.ONCLICK == event.getTypeInt()) {
                    onButtonClick(elementId);
                }
            }
        });
    }

    private void onButtonClick(String elementId) {
        if ("_fullprevious".equals(elementId)) {
            onFullPrevious();
        } else if ("_previous".equals(elementId)) {
            onPrevious();
        } else if ("_next".equals(elementId)) {
            onNext();
        }else if ("_fullnext".equals(elementId)) {
            onFullNext();
        } else {
            onInstanceClick(formInstances.get(elementId));
        }
    }

    private void onInstanceClick(FormInstance instance) {
        Preconditions.checkNotNull(instance);
        // todo
    }

    private void onFullNext() {
        // todo
    }

    private void onNext() {
        // todo
    }

    private void onPrevious() {
        // todo
    }

    private void onFullPrevious() {
        // todo
    }

    private String perviousButtons() {
        return "<li><a href='javascript:;' id='_fullprevious'>&laquo;</a></li>\n" +
                "<li><a href='javascript:;' id='_previous'>&lt;</a></li>";
    }

    private String nextButtons() {
        return "<li><a href='javascript:;' id='_next'>&gt;</a></li>\n" +
                "<li><a href='javascript:;' id='_fullnext'>&raquo;</a></li>";
    }

    public int getTabCount() {
        return tabCount;
    }

    public void setTabCount(int tabCount) {
        this.tabCount = tabCount;
    }
}
