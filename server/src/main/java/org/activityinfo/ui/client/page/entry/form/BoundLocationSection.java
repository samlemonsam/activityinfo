package org.activityinfo.ui.client.page.entry.form;

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

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.legacy.shared.command.CreateLocation;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.ui.client.page.entry.admin.AdminComboBox;
import org.activityinfo.ui.client.page.entry.admin.AdminComboBoxSet;
import org.activityinfo.ui.client.page.entry.admin.AdminFieldSetPresenter;

/**
 * Presents a form dialog for a Site for an Activity that has a LocationType
 * that is bound to an AdminLevel
 */
public class BoundLocationSection extends FormSectionWithFormLayout<SiteDTO> implements LocationFormSection {

    private final Dispatcher dispatcher;

    private AdminFieldSetPresenter adminFieldSet;
    private AdminComboBoxSet comboBoxes;
    private BoundAdminComboBox leafComboBox;

    private LocationDTO location;

    public BoundLocationSection(Dispatcher dispatcher, ActivityFormDTO activity) {

        this.dispatcher = dispatcher;

        adminFieldSet = new AdminFieldSetPresenter(dispatcher,
                activity.getBounds(),
                activity.getAdminLevels());

        comboBoxes = new AdminComboBoxSet(adminFieldSet, new BoundAdminComboBox.Factory());

        for (AdminComboBox comboBox : comboBoxes) {
            add(comboBox.asWidget());
            leafComboBox = (BoundAdminComboBox) comboBox;
        }

    }

    @Override
    public void updateForm(LocationDTO location, boolean isNew) {
        this.location = location;
        adminFieldSet.setSelection(location);
    }

    @Override
    public void save(final AsyncCallback<Void> callback) {
        if (isDirty()) {
            newLocation();
            dispatcher.execute(new CreateLocation(location), new AsyncCallback<VoidResult>() {

                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(VoidResult result) {
                    callback.onSuccess(null);
                }
            });
        } else {
            callback.onSuccess(null);
        }
    }

    private void newLocation() {
        location = new LocationDTO(location);
        location.setId(new KeyGenerator().generateInt());
        location.setName(leafComboBox.getValue().getName());
        for (AdminLevelDTO level : adminFieldSet.getAdminLevels()) {
            location.setAdminEntity(level.getId(), adminFieldSet.getAdminEntity(level));
        }
    }

    private boolean isDirty() {
        for (AdminLevelDTO level : adminFieldSet.getAdminLevels()) {
            AdminEntityDTO original = location.getAdminEntity(level.getId());
            AdminEntityDTO current = adminFieldSet.getAdminEntity(level);

            if (current == null && original != null) {
                return true;
            }
            if (current != null && original == null) {
                return true;
            }
            if (current.getId() != original.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean validate() {
        return comboBoxes.validate();
    }

    @Override
    public void updateModel(SiteDTO m) {
        m.setLocation(location);
    }

    @Override
    public void updateForm(SiteDTO m, boolean isNew) {

    }
}
