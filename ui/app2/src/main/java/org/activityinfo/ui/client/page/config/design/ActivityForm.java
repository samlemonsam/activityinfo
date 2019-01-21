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
package org.activityinfo.ui.client.page.config.design;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.Published;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.page.config.form.AbstractDesignForm;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;
import org.activityinfo.ui.client.widget.legacy.MappingComboBox;
import org.activityinfo.ui.client.widget.legacy.MappingComboBoxBinding;
import org.activityinfo.ui.client.widget.legacy.OnlyValidFieldBinding;

import java.util.Objects;

/**
 * FormClass for editing ActivityDTO
 */
class ActivityForm extends AbstractDesignForm {

    private FormBinding binding;
    private String categoryLink = "http://help.activityinfo.org/m/28175/l/842935-folders-in-activityinfo";
    private final LabelField publishedTooltip = new LabelField();

    /**
     * Creates an info button (with icon) which navigates to a given hyperlink on press
     */
    private class HyperlinkInfoButton extends Button {

        public HyperlinkInfoButton(String displayText, String link) {
            this.setIcon(IconImageBundle.ICONS.info());
            this.addListener(Events.OnClick, new Listener<BaseEvent>() {
                @Override
                public void handleEvent(BaseEvent baseEvent) {
                    Window.open(link,"_blank",null);
                }
            });
            this.setHtml(SafeHtmlUtils.fromString(displayText));
        }

    }

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

        final LocationTypeComboBox locationTypeCombo = new LocationTypeComboBox(service, database.getCountry());
        this.add(locationTypeCombo);

        binding.addFieldBinding(new LocationTypeFieldBinding(database.getCountry(), locationTypeCombo, "locationTypeId"));

        final MappingComboBox frequencyCombo = new MappingComboBox();
        frequencyCombo.setAllowBlank(false);
        frequencyCombo.setFieldLabel(I18N.CONSTANTS.reportingFrequency());
        frequencyCombo.add(ActivityFormDTO.REPORT_ONCE, I18N.CONSTANTS.reportOnce());
        frequencyCombo.add(ActivityFormDTO.REPORT_MONTHLY, I18N.CONSTANTS.monthly());

        binding.addFieldBinding(new MappingComboBoxBinding(frequencyCombo, "reportingFrequency"));
        this.add(frequencyCombo);

        final MappingComboBox publishedCombo = new MappingComboBox();
        publishedCombo.setAllowBlank(false);
        publishedCombo.setFieldLabel(I18N.CONSTANTS.visibility());
        publishedCombo.add(Published.NOT_PUBLISHED.getIndex(), I18N.CONSTANTS.privateVisibility());
        publishedCombo.add(Published.ALL_ARE_PUBLISHED.getIndex(), I18N.CONSTANTS.publicVisibility());
        publishedCombo.addListener(Events.SelectionChange, this::updateTooltip);

        binding.addFieldBinding(new MappingComboBoxBinding(publishedCombo, "published"));

        binding.addListener(Events.Bind, (Listener<BindingEvent>) be -> {
            // User should not be able to change reporting frequency after creation
            frequencyCombo.setEnabled(!isSaved(be.getModel()));

            // User should only be able to change visibility (i.e. public or private) after creation
            publishedCombo.setVisible(isSaved(be.getModel()));
            publishedTooltip.setVisible(isSaved(be.getModel()));
        });

        this.add(publishedCombo);
        this.add(publishedTooltip);

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

    private void updateTooltip(BaseEvent se) {
        if (se == null || !(se instanceof SelectionChangedEvent)) {
            return;
        }
        SelectionChangedEvent selection = (SelectionChangedEvent) se;
        if (selection.getSelectedItem() == null) {
            return;
        }
        Object value = selection.getSelectedItem().get("value");
        if (Objects.equals(value, Published.NOT_PUBLISHED.getIndex())) {
            publishedTooltip.setText(I18N.CONSTANTS.privateVisibilityTooltip());
        } else if (Objects.equals(value, Published.ALL_ARE_PUBLISHED.getIndex())) {
            publishedTooltip.setText(I18N.CONSTANTS.publicVisibilityTooltip());
        }
    }

    @Override
    public FormBinding getBinding() {
        return binding;
    }

    private boolean isSaved(ModelData model) {
        return model.get("id") != null;
    }
}
