package org.activityinfo.ui.client.component.formdesigner.properties;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellBrowser;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.widget.ModalDialog;

import java.util.List;
import java.util.Set;

public class ChooseFormDialog extends Composite {

    private static OurUiBinder uiBinder = GWT.create(OurUiBinder.class);

    interface OurUiBinder extends UiBinder<Widget, ChooseFormDialog> {
    }

    private final MultiSelectionModel<CatalogEntry> selectionModel = 
            new MultiSelectionModel<>(ChooseFormTreeModel.KEY_PROVIDER);
    private final ModalDialog dialog;

    @UiField(provided = true)
    CellBrowser browser;

    public ChooseFormDialog(ResourceLocator resourceLocator) {
        browser = createBrowser(resourceLocator);

        initWidget(uiBinder.createAndBindUi(this));

        dialog = new ModalDialog(this, I18N.CONSTANTS.chooseForm());
        dialog.getDialogDiv().getStyle().setWidth(800, Style.Unit.PX);
        dialog.getPrimaryButton().setEnabled(false);
        dialog.getPrimaryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
               if (!getSelectedLeafNodes().isEmpty()) {
                   dialog.hide();
               }
            }
        });

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                dialog.getPrimaryButton().setEnabled(!getSelectedLeafNodes().isEmpty());
            }
        });
    }

    private Set<CatalogEntry> getSelectedLeafNodes() {
        Set<CatalogEntry> set = Sets.newHashSet();
        for (CatalogEntry node : selectionModel.getSelectedSet()) {
            if (node.getType() == CatalogEntryType.FORM) {
                set.add(node);
            }
        }
        return set;
    }

    private CellBrowser createBrowser(ResourceLocator resourceLocator) {
        ChooseFormTreeModel model = new ChooseFormTreeModel(resourceLocator, selectionModel);
        return new CellBrowser.Builder<CatalogEntry>(model, null)
                .loadingIndicator(new Label(I18N.CONSTANTS.loading()))
                .pageSize(9999)
                .build();
    }

    public void choose(final AsyncCallback<CatalogEntry> callback) {
        final HandlerRegistration registration = dialog.getPrimaryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Set<CatalogEntry> nodes = getSelectedLeafNodes();
                if (!nodes.isEmpty()) {
                    dialog.hide();
                    callback.onSuccess(nodes.iterator().next());
                }
            }
        });
        dialog.show();
    }

    public List<ResourceId> getFormClassIds() {
        List<ResourceId> ids = Lists.newArrayList();
        for (CatalogEntry node : getSelectedLeafNodes()) {
            ids.add(ResourceId.valueOf(node.getId()));
        }
        return ids;
    }
}
