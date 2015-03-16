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
import org.activityinfo.core.shared.criteria.ParentCriteria;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.widget.LoadingPanel;
import org.activityinfo.ui.client.widget.ModalDialog;

import javax.inject.Provider;
import java.util.List;

/**
 * @author yuriyz on 02/04/2015.
 */
public class SelectSubformTypeDialog {

    private static final int DIALOG_WIDTH = 900;

    private final FormDesigner formDesigner;
    private final ResourceId parentId;
    private final ModalDialog dialog;
    private final SelectSubformTypePanel contentPanel;
    private final LoadingPanel<List<FormInstance>> loadingPanel;

    public SelectSubformTypeDialog(ResourceId parentId, FormDesigner formDesigner) {
        this.parentId = parentId;
        this.formDesigner = formDesigner;
        this.contentPanel = new SelectSubformTypePanel(parentId) {
            @Override
            public void stateChanged() {
                dialog.getPrimaryButton().setEnabled(contentPanel.isValid());
            }
        };

        this.loadingPanel = new LoadingPanel<>();
        this.loadingPanel.setDisplayWidget(contentPanel);

        this.dialog = new ModalDialog(loadingPanel);
        this.dialog.setDialogTitle(I18N.CONSTANTS.selectType());
        this.dialog.getDialogDiv().getStyle().setWidth(DIALOG_WIDTH, Style.Unit.PX);
        this.dialog.getPrimaryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (contentPanel.isValid()) {
                    dialog.hide();
                } else {
                    dialog.getStatusLabel().setText(I18N.CONSTANTS.pleaseSelectType());
                }
            }
        });
    }

    public ResourceId getSelectedClassId() {
        return contentPanel.getSelectedClassId();
    }

    public void setHideHandler(ClickHandler hideHandler) {
        dialog.setHideHandler(hideHandler);
    }

    public void show() {
        this.loadingPanel.show(new Provider<Promise<List<FormInstance>>>() {

            @Override
            public Promise<List<FormInstance>> get() {
                // restricted by activity form class (means by db of that activity but we don't want to mess code with legacy here,
                // so deal with it in QueryExecutor)
                ResourceId restrictedBy = formDesigner.getModel().getRootFormClass().getId();
                ParentCriteria criteria = ParentCriteria.isChildOf(parentId, restrictedBy);
                return formDesigner.getResourceLocator().queryInstances(criteria);
            }
        });
        dialog.show();
    }
}
