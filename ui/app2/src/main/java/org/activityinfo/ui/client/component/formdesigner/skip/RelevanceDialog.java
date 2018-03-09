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
package org.activityinfo.ui.client.component.formdesigner.skip;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormField;
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;
import org.activityinfo.ui.client.component.formdesigner.properties.FieldEditor;
import org.activityinfo.ui.client.widget.ModalDialog;

/**
 * @author yuriyz on 7/23/14.
 */
public class RelevanceDialog {

    public static final int DIALOG_WIDTH = 900;

    private final FormField formField;
    private final ModalDialog dialog;
    private final RelevancePanelPresenter presenter;

    public RelevanceDialog(final FieldWidgetContainer fieldWidgetContainer, final FieldEditor propertiesPresenter) {
        this.formField = fieldWidgetContainer.getFormField();
        this.presenter = new RelevancePanelPresenter(fieldWidgetContainer);
        this.dialog = new ModalDialog(presenter.getView());
        this.dialog.setDialogTitle(I18N.CONSTANTS.defineRelevanceLogic());
        this.dialog.getDialogDiv().getStyle().setWidth(DIALOG_WIDTH, Style.Unit.PX);
        this.dialog.getPrimaryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                presenter.updateFormField();
                propertiesPresenter.setRelevanceState(formField, true);
                dialog.hide();
            }
        });
    }

    public void show() {
        dialog.show();
    }
}
