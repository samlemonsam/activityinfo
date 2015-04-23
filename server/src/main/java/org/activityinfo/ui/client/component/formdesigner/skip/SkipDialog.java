package org.activityinfo.ui.client.component.formdesigner.skip;
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

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormField;
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;
import org.activityinfo.ui.client.component.formdesigner.properties.PropertiesPresenter;
import org.activityinfo.ui.client.widget.ModalDialog;

/**
 * @author yuriyz on 7/23/14.
 */
public class SkipDialog {

    public static final int DIALOG_WIDTH = 900;
//    public static final int DIALOG_HEIGHT = 800;

    private final FieldWidgetContainer fieldWidgetContainer;
    private final FormField formField;
    private final ModalDialog dialog;
    private final SkipPanelPresenter skipPanelPresenter;

    public SkipDialog(final FieldWidgetContainer fieldWidgetContainer, final PropertiesPresenter propertiesPresenter) {
        this.fieldWidgetContainer = fieldWidgetContainer;
        this.formField = fieldWidgetContainer.getFormField();
        this.skipPanelPresenter = new SkipPanelPresenter(fieldWidgetContainer);
        this.dialog = new ModalDialog(skipPanelPresenter.getView());
        this.dialog.setDialogTitle(I18N.CONSTANTS.defineRelevanceLogic());
        this.dialog.getDialogDiv().getStyle().setWidth(DIALOG_WIDTH, Style.Unit.PX);
//        this.dialog.getDialogDiv().getStyle().setHeight(DIALOG_HEIGHT, Style.Unit.PX);
        this.dialog.getPrimaryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                skipPanelPresenter.updateFormField();
                propertiesPresenter.setRelevanceState(formField, false);
                dialog.hide();
            }
        });
    }

    public void show() {
        dialog.show();
    }
}
