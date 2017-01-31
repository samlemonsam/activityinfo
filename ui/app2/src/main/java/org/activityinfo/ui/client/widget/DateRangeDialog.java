package org.activityinfo.ui.client.widget;
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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.activityinfo.i18n.shared.I18N;

/**
 * @author yuriyz on 07/06/2015.
 */
public class DateRangeDialog {

    private final ModalDialog dialog = new ModalDialog();
    private final DateRangePanel panel = new DateRangePanel();

    private ClickHandler successCallback;

    public DateRangeDialog() {
        dialog.setDialogTitle(I18N.CONSTANTS.customDateRange());
        dialog.getModalBody().add(panel);
        panel.inlineForm(true);

        dialog.getPrimaryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onOk();
            }
        });
    }

    private void onOk() {
        if (panel.validate()) {
            dialog.hide();
            if (successCallback != null) {
                successCallback.onClick(null);
            }
        }
    }


    public ClickHandler getSuccessCallback() {
        return successCallback;
    }

    public void setSuccessCallback(ClickHandler successCallback) {
        this.successCallback = successCallback;
    }
}
