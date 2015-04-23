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

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.command.CreateLocation;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.ui.client.page.entry.location.LocationDialog;
import org.activityinfo.ui.client.page.entry.location.LocationDialog.Callback;
import org.activityinfo.ui.client.widget.coord.CoordinateFields;

import java.util.Map;
import java.util.Map.Entry;

public class LocationSection extends FormSectionWithFormLayout<SiteDTO> implements LocationFormSection {

    private boolean isNew;
    private ActivityFormDTO activity;
    private LocationDTO location;
    private Dispatcher dispatcher;
    private LabelField nameField;
    private LabelField axeField;
    private CoordinateFields coordinateFields;
    private Map<Integer, LabelField> levelFields;

    public LocationSection(Dispatcher dispatcher, ActivityFormDTO activity) {
        this.dispatcher = dispatcher;
        this.activity = activity;

        levelFields = Maps.newHashMap();
        for (AdminLevelDTO level : activity.getAdminLevels()) {
            LabelField levelField = new LabelField();
            levelField.setFieldLabel(level.getName());
            add(levelField);
            levelFields.put(level.getId(), levelField);
        }

        nameField = new LabelField();
        nameField.setFieldLabel(activity.getLocationType().getName());
        add(nameField);

        axeField = new LabelField();
        axeField.setFieldLabel(I18N.CONSTANTS.axe());
        add(axeField);

        coordinateFields = new CoordinateFields();
        coordinateFields.setReadOnly(true);
        add(coordinateFields.getLatitudeField());
        add(coordinateFields.getLongitudeField());

        Button changeLocation = new Button(I18N.CONSTANTS.changeLocation(), new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                changeLocation();
            }
        });
        add(changeLocation);

    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void updateForm(LocationDTO location, boolean isNew) {
        this.location = location;
        this.isNew = isNew;
        nameField.setValue(location.getName());
        axeField.setValue(location.getAxe());

        for (Entry<Integer, LabelField> entry : levelFields.entrySet()) {
            AdminEntityDTO entity = location.getAdminEntity(entry.getKey());
            entry.getValue().setValue(entity == null ? null : entity.getName());
        }

        if (location.hasCoordinates()) {
            coordinateFields.getLatitudeField().setValue(location.getLatitude());
            coordinateFields.getLongitudeField().setValue(location.getLongitude());
        } else {
            coordinateFields.setValue(null);
        }
    }

    @Override
    public void updateModel(SiteDTO site) {
        site.setLocationId(location.getId());
    }

    @Override
    public void save(final AsyncCallback<Void> callback) {
        if (!isNew) {
            callback.onSuccess(null);
        } else {
            dispatcher.execute(new CreateLocation(location), new AsyncCallback<VoidResult>() {

                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(VoidResult result) {
                    isNew = false;
                    callback.onSuccess(null);
                }
            });
        }
    }

    @Override
    public void updateForm(SiteDTO m, boolean isNew) {
    }

    private void changeLocation() {
        LocationDialog dialog = new LocationDialog(dispatcher, activity.getLocationType());
        dialog.show(new Callback() {

            @Override
            public void onSelected(LocationDTO location, boolean isNew) {
                updateForm(location, isNew);
            }
        });
    }

}
