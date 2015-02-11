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
import org.activityinfo.ui.client.widget.ClickHandler;

import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 02/04/2015.
 */
public class SubFormTabsPresenter {

    public static enum ButtonType {
        FULL_PREVIOUS("_fullprevious"),
        PREVIOUS("_previous"),
        NEXT("_next"),
        FULL_NEXT("_fullnext");

        private final String value;

        ButtonType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ButtonType fromValue(String value) {
            for (ButtonType type : values()) {
                if (type.getValue().equals(value)) {
                    return type;
                }
            }
            return null;
        }
    }

    private final SubFormTabs view;

    private int tabCount = SubformConstants.DEFAULT_TAB_COUNT;

    private final Map<String, FormInstance> formInstances = Maps.newHashMap();
    private final List<HandlerRegistration> clickHandlers = Lists.newArrayList();

    private ClickHandler<ButtonType> moveButtonClickHandler;
    private ClickHandler<FormInstance> instanceTabClickHandler;

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
        for (ButtonType buttonType : ButtonType.values()) {
            addClickHandlerToElementById(buttonType.getValue());
        }

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
        ButtonType buttonType = ButtonType.fromValue(elementId);
        if (buttonType != null) {
            if (moveButtonClickHandler != null) {
                moveButtonClickHandler.onClick(buttonType);
            }
        } else {
            FormInstance instance = formInstances.get(elementId);
            Preconditions.checkNotNull(instance);
            if (instanceTabClickHandler != null) {
                instanceTabClickHandler.onClick(instance);
            }
        }
    }

    public void setMoveButtonClickHandler(ClickHandler<ButtonType> moveButtonClickHandler) {
        this.moveButtonClickHandler = moveButtonClickHandler;
    }

    public void setInstanceTabClickHandler(ClickHandler<FormInstance> instanceTabClickHandler) {
        this.instanceTabClickHandler = instanceTabClickHandler;
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

    public void setTabCountSafely(String tabCount) {
        try {
            setTabCountSafely((int) Double.parseDouble(tabCount));
        } catch (Exception e) {
            setTabCountSafely(SubformConstants.DEFAULT_TAB_COUNT);
        }
    }

    public void setTabCountSafely(int tabCount) {
        if (tabCount < SubformConstants.MIN_TAB_COUNT) {
            tabCount = SubformConstants.MIN_TAB_COUNT;
        }
        if (tabCount > SubformConstants.MAX_TAB_COUNT) {
            tabCount = SubformConstants.MAX_TAB_COUNT;
        }
        setTabCount(tabCount);
    }

    public void setTabCount(int tabCount) {
        Preconditions.checkState(tabCount >= SubformConstants.MIN_TAB_COUNT && tabCount <= SubformConstants.MAX_TAB_COUNT);
        this.tabCount = tabCount;
    }
}
