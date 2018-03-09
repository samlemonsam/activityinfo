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
package org.activityinfo.ui.client.input.view;

import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.ui.client.store.FormStore;

public class FormDialog {

    private ResourceId formId;
    private FormInputView formInputView;
    private final Dialog dialog;

    public FormDialog(FormStore formStore, RecordRef recordRef) {
        this.formId = formId;

        FormInputView panel = new FormInputView(formStore, recordRef);

        dialog = new Dialog();
        dialog.setHeading(I18N.CONSTANTS.form());
        dialog.setPixelSize(640, 480);
        dialog.setModal(true);
        dialog.setWidget(panel);
        dialog.getButton(Dialog.PredefinedButton.OK).addSelectHandler(new SelectEvent.SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                panel.save(closeEvent -> {
                   dialog.hide();
                });
            }
        });
    }

    public void show() {
        dialog.show();
        dialog.center();
    }

}
