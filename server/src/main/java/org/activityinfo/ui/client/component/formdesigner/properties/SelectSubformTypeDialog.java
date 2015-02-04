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

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.widget.ModalDialog;

/**
 * @author yuriyz on 02/04/2015.
 */
public class SelectSubformTypeDialog {

    private static final int DIALOG_WIDTH = 900;

    private final ModalDialog dialog;
    private ResourceId selectedClassId = null;

    public SelectSubformTypeDialog() {
        HTML html = new HTML("<h2>Not implemented yet.</h2>");
        this.dialog = new ModalDialog(html) ;
        this.dialog.setDialogTitle(I18N.CONSTANTS.selectType());
        this.dialog.getDialogDiv().getStyle().setWidth(DIALOG_WIDTH, Style.Unit.PX);
        this.dialog.disablePrimaryButton();
        this.dialog.getPrimaryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialog.hide();
            }
        });
    }

    public ResourceId getSelectedClassId() {
        return selectedClassId;
    }

    public void setHideHandler(ClickHandler hideHandler) {
        dialog.setHideHandler(hideHandler);
    }

    public void show() {
        dialog.show();
    }
}
