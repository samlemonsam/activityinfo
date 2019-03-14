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
package org.activityinfo.ui.client.page.entry.form.field;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ModelPropertyRenderer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.PartnerDTO;

import java.util.List;

public class PartnerComboBox extends ComboBox<PartnerDTO> {

    public PartnerComboBox(List<PartnerDTO> partners) {

        final ListStore<PartnerDTO> store = new ListStore<PartnerDTO>();
        store.add(partners);
        store.sort("name", Style.SortDir.ASC);

        setName("partner");
        setDisplayField("name");
        setEditable(false);
        setTriggerAction(ComboBox.TriggerAction.ALL);
        setStore(store);
        setFieldLabel(I18N.CONSTANTS.partner());
        setForceSelection(true);
        setAllowBlank(false);
        setItemRenderer(new MultilineRenderer<>(new ModelPropertyRenderer<PartnerDTO>("name")));

        if (store.getCount() == 1) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    setValue(store.getAt(0));
                }
            });
        }
    }

}
