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
package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.observable.Observable;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.viewModel.PermissionFilters;
import org.activityinfo.ui.client.lookup.view.LevelWidget;
import org.activityinfo.ui.client.lookup.viewModel.LookupKeyViewModel;
import org.activityinfo.ui.client.lookup.viewModel.LookupViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class ReferenceFieldWidget implements FieldWidget {

    private static final Logger LOGGER = Logger.getLogger(ReferenceFieldWidget.class.getName());

    private final LookupViewModel viewModel;
    private final List<LevelWidget> levelWidgets = new ArrayList<>();
    private final Widget widget;
    private FieldUpdater fieldUpdater;

    public ReferenceFieldWidget(FormSource formSource,
                                FormTree formTree,
                                FormField field,
                                PermissionFilters filters,
                                FieldUpdater fieldUpdater) {

        this.fieldUpdater = fieldUpdater;
        this.viewModel = new LookupViewModel(formSource, formTree, field,
                Observable.just(filters.getReferenceBaseFilter(field.getId())));

        for (LookupKeyViewModel level : viewModel.getLookupKeys()) {
            LevelWidget widget = new LevelWidget(viewModel, level);
            if(level.isLeaf()) {
                widget.addSelectionHandler(this::onSelection);
            }
            levelWidgets.add(widget);
        }

        if (levelWidgets.size() == 0) {
            // If we have a reference field with _no_ levels, then the form is inaccessible
            widget = inaccessible(field);
        } else if(levelWidgets.size() == 1) {
            // If we have a single lookup key, then just show a single combobox
            widget = levelWidgets.get(0).getComboBox();

        } else {
            // Otherwise we need labeled rows of comboboxes
            CssFloatLayoutContainer panel = new CssFloatLayoutContainer();
            for (LevelWidget levelWidget : levelWidgets) {
                panel.add(levelWidget, new CssFloatLayoutContainer.CssFloatData(1, new Margins(5, 0, 5, 0)));
            }
            widget = panel;
        }

        for (LevelWidget levelWidget : levelWidgets) {
            levelWidget.addBlurHandler(event -> fieldUpdater.touch());
        }
    }

    private Widget inaccessible(FormField field) {
        SafeHtmlBuilder inaccessibleForms = new SafeHtmlBuilder();
        ReferenceType referenceType = (ReferenceType) field.getType();
        inaccessibleForms.appendHtmlConstant("<span style='color:red'>");
        inaccessibleForms.appendHtmlConstant("<b>");
        inaccessibleForms.appendEscaped(I18N.CONSTANTS.refFormsNotAccessible());
        inaccessibleForms.appendEscaped(":");
        inaccessibleForms.appendHtmlConstant("</b>");
        for (ResourceId referenceFormId : referenceType.getRange()) {
            inaccessibleForms.appendHtmlConstant("<br>");
            inaccessibleForms.appendEscaped(referenceFormId.asString());
        }
        inaccessibleForms.appendHtmlConstant("</span>");
        HTML inaccessibleMesage = new HTML(inaccessibleForms.toSafeHtml());
        return inaccessibleMesage;
    }

    private void onSelection(SelectionEvent<String> event) {
        LOGGER.info("onSelection: " + event.getSelectedItem());
        viewModel.getSelectedRecords().once().then(new AsyncCallback<Set<RecordRef>>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(Set<RecordRef> result) {
                if (result.isEmpty()) {
                    fieldUpdater.update(FieldInput.EMPTY);
                } else {
                    fieldUpdater.update(new FieldInput(new ReferenceValue(result)));
                }
            }
        });
    }

    @Override
    public void init(FieldValue value) {
        ReferenceValue referenceValue = (ReferenceValue) value;
        RecordRef recordRef = referenceValue.getOnlyReference();
        viewModel.setInitialSelection(referenceValue.getReferences());
    }

    @Override
    public void clear() {
        viewModel.clearSelection();
    }

    @Override
    public void setRelevant(boolean relevant) {
        for (LevelWidget levelWidget : levelWidgets) {
            levelWidget.setRelevant(relevant);
        }
    }

    @Override
    public void focus() {
        levelWidgets.get(0).getComboBox().focus();
    }

    @Override
    public Widget asWidget() {
        return widget;
    }
}
