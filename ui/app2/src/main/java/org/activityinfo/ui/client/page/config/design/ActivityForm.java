package org.activityinfo.ui.client.page.config.design;

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

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BindingEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.Published;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.widget.legacy.MappingComboBox;
import org.activityinfo.ui.client.widget.legacy.MappingComboBoxBinding;
import org.activityinfo.ui.client.widget.legacy.OnlyValidFieldBinding;
import org.activityinfo.ui.client.widget.legacy.RemoteComboBox;

/**
 * FormClass for editing ActivityDTO
 */
class ActivityForm extends AbstractDesignForm {

    private FormBinding binding;

    public ActivityForm(Dispatcher service, UserDatabaseDTO database) {
        super();

        binding = new FormBinding(this);

        this.setHeaderVisible(false);
        this.setScrollMode(Scroll.AUTOY);
        this.setLabelWidth(150);
        this.setBorders(false);

        final NumberField idField = new NumberField();
        idField.setFieldLabel("ID");
        idField.setReadOnly(true);
        binding.addFieldBinding(new FieldBinding(idField, "id"));
        add(idField);

        TextField<String> nameField = new TextField<String>();
        nameField.setAllowBlank(false);
        nameField.setFieldLabel(I18N.CONSTANTS.name());
        nameField.setMaxLength(ActivityFormDTO.NAME_MAX_LENGTH);
        nameField.setValidator(new BlankValidator());

        binding.addFieldBinding(new OnlyValidFieldBinding(nameField, "name"));
        this.add(nameField);

        TextField<String> categoryField = new TextField<String>();
        categoryField.setFieldLabel(I18N.CONSTANTS.category());
        categoryField.setMaxLength(ActivityFormDTO.CATEGORY_MAX_LENGTH);
        binding.addFieldBinding(new OnlyValidFieldBinding(categoryField, "category"));
        add(categoryField);

        final ComboBox<LocationTypeEntry> locationTypeCombo = new RemoteComboBox<>();
        locationTypeCombo.setStore(LocationTypeProxy.createStore(service, database.getCountry().getId()));
        locationTypeCombo.setAllowBlank(false);
        locationTypeCombo.setTriggerAction(ComboBox.TriggerAction.ALL);
        locationTypeCombo.setFieldLabel(I18N.CONSTANTS.locationType());
        locationTypeCombo.setRenderer(new LocationTypeListRenderer());
        locationTypeCombo.setItemSelector(LocationTypeListRenderer.getItemSelector());
        locationTypeCombo.setValueField("id");
        locationTypeCombo.setDisplayField("label");
        this.add(locationTypeCombo);

        binding.addFieldBinding(new LocationTypeFieldBinding(database.getCountry(), locationTypeCombo, "locationTypeId"));

        final MappingComboBox frequencyCombo = new MappingComboBox();
        frequencyCombo.setAllowBlank(false);
        frequencyCombo.setFieldLabel(I18N.CONSTANTS.reportingFrequency());
        frequencyCombo.add(ActivityFormDTO.REPORT_ONCE, I18N.CONSTANTS.reportOnce());
        frequencyCombo.add(ActivityFormDTO.REPORT_MONTHLY, I18N.CONSTANTS.monthly());

        binding.addFieldBinding(new MappingComboBoxBinding(frequencyCombo, "reportingFrequency"));
        this.add(frequencyCombo);

        MappingComboBox publishedCombo = new MappingComboBox();
        publishedCombo.setAllowBlank(false);
        publishedCombo.setFieldLabel(I18N.CONSTANTS.published());
        publishedCombo.add(Published.NOT_PUBLISHED.getIndex(), I18N.CONSTANTS.notPublished());
        publishedCombo.add(Published.ALL_ARE_PUBLISHED.getIndex(), I18N.CONSTANTS.allArePublished());
        binding.addFieldBinding(new MappingComboBoxBinding(publishedCombo, "published"));

        binding.addListener(Events.Bind, new Listener<BindingEvent>() {

            @Override
            public void handleEvent(BindingEvent be) {
                //                locationTypeCombo.setEnabled(!isSaved(be.getModel()));
                frequencyCombo.setEnabled(!isSaved(be.getModel()));
            }
        });

        this.add(publishedCombo);

        getBinding().addListener(Events.Bind, new Listener<BindingEvent>() {

            @Override
            public void handleEvent(BindingEvent be) {
                ActivityDTO activity = (ActivityDTO) be.getModel();
                locationTypeCombo.setVisible(activity.getClassicView());
                frequencyCombo.setVisible(activity.getClassicView());
                ActivityForm.this.setHeadingText(activity.getClassicView() ?
                        I18N.CONSTANTS.newActivity() :
                        I18N.CONSTANTS.newForm());
            }
        });

        hideFieldWhenNull(idField);
    }

    @Override
    public FormBinding getBinding() {
        return binding;
    }

    private boolean isSaved(ModelData model) {
        return model.get("id") != null;
    }
}
