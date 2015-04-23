package org.activityinfo.ui.client.page.config.design.dialog;
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

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.widget.ModalDialog;

/**
 * @author yuriyz on 11/05/2014.
 */
public class NewFormDialog {

    public static final String CLASSIC_VIEW_EXPLANATION_URL = "http://activity-info.screenstepslive.com/s/8956/m/28175/l/276541-difference-between-classic-mode-and-new-design-mode";

    public static enum ViewType {
        CLASSIC, CLASSIC_MONTHLY, NEW_FORM_DESIGNER
    }

    private final ModalDialog dialog;
    private final NewFormDialogPanel content;
    private ClickHandler successHandler;

    public NewFormDialog() {
        this.content = new NewFormDialogPanel();
        this.dialog = new ModalDialog(content);
        this.dialog.setDialogTitle(I18N.CONSTANTS.newForm());
        this.dialog.getPrimaryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (validate()) {
                    dialog.hide();
                    if (successHandler != null) {
                        successHandler.onClick(event);
                    }
                }
            }
        });

        content.getNameField().addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                validate();
            }
        });
        content.getFormDesigner().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                validate();
            }
        });
        content.getClassic().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                validate();
            }
        });
        validate();
    }

    public boolean validate() {
        boolean isNameValid = !Strings.isNullOrEmpty(getName());

        content.getNameFieldGroup().setShowValidationMessage(!isNameValid);

        dialog.getPrimaryButton().setEnabled(isNameValid);
        return isNameValid;
    }

    public ViewType getViewType() {
        if (content.getClassic().getValue()) {
            return ViewType.CLASSIC;
        } else if (content.getFormDesigner().getValue()) {
            return ViewType.NEW_FORM_DESIGNER;
        }
        return null;
    }

    public NewFormDialog show() {
        dialog.show();
        return this;
    }

    public String getName() {
        return content.getNameField().getValue();
    }

    public String getCategory() {
        return content.getCategoryField().getValue();
    }

    public ClickHandler getSuccessHandler() {
        return successHandler;
    }

    public void setSuccessHandler(ClickHandler successHandler) {
        this.successHandler = successHandler;
    }
}
