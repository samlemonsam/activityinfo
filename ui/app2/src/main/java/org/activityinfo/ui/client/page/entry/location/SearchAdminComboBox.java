/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.page.entry.location;

import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.legacy.shared.model.AdminLevelDTO;
import org.activityinfo.ui.client.page.entry.admin.AdminComboBox;
import org.activityinfo.ui.client.page.entry.admin.AdminComboBoxSet.ComboBoxFactory;
import org.activityinfo.ui.client.page.entry.form.resources.SiteFormResources;

public class SearchAdminComboBox extends ComboBox<AdminEntityDTO> implements AdminComboBox {

    private Element clearSpan;
    private final AdminLevelDTO level;

    public SearchAdminComboBox(AdminLevelDTO level, ListStore<AdminEntityDTO> store) {
        this.level = level;
        setFieldLabel(level.getName());
        setStore(store);
        setTypeAhead(false);
        setForceSelection(true);
        setEditable(false);
        setValueField("id");
        setUseQueryCache(false);
        setDisplayField("name");
        setTriggerAction(TriggerAction.ALL);
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);

        clearSpan = DOM.createSpan();
        clearSpan.setInnerText(I18N.CONSTANTS.clear());
        clearSpan.addClassName(SiteFormResources.INSTANCE.style().adminClearSpan());
        clearSpan.getStyle().setVisibility(Style.Visibility.VISIBLE);

        DOM.sinkEvents(clearSpan, Event.MOUSEEVENTS);

        getElement().appendChild(clearSpan);
    }

    public AdminLevelDTO getLevel() {
        return level;
    }

    @Override
    public void setValue(AdminEntityDTO value) {
        super.setValue(value);

        this.clearSpan.getStyle().setVisibility(this.value == null ?
                Style.Visibility.HIDDEN :
                Style.Visibility.VISIBLE);
    }

    @Override
    protected void onClick(ComponentEvent ce) {
        if (clearSpan.isOrHasChild(ce.getTarget())) {
            setValue(null);
        }
        super.onClick(ce);
    }

    @Override
    protected void onKeyDown(FieldEvent fe) {
        super.onKeyDown(fe);
        if (fe.getKeyCode() == KeyCodes.KEY_ESCAPE) {
            setValue(null);
        }
    }

    @Override
    public void addSelectionChangeListener(Listener<SelectionChangedEvent> listener) {
        addListener(Events.SelectionChange, listener);
    }

    public static class Factory implements ComboBoxFactory {

        @Override
        public AdminComboBox create(AdminLevelDTO level, ListStore<AdminEntityDTO> store) {
            return new SearchAdminComboBox(level, store);
        }
    }
}
