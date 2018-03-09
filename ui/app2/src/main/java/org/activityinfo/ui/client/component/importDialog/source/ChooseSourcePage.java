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
package org.activityinfo.ui.client.component.importDialog.source;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.component.importDialog.ImportPage;
import org.activityinfo.ui.client.component.importDialog.PageChangedEvent;
import org.activityinfo.ui.client.component.importDialog.model.ImportModel;
import org.activityinfo.ui.client.component.importDialog.model.source.PastedTable;
import org.activityinfo.ui.client.dispatch.type.JsConverterFactory;
import org.activityinfo.ui.client.widget.TextArea;

/**
 * Start page for the table import process that prompts the user
 * for pasted table data
 */
public class ChooseSourcePage extends ResizeComposite implements ImportPage {


    private static ChoosePageUiBinder uiBinder = GWT
            .create(ChoosePageUiBinder.class);

    interface ChoosePageUiBinder extends UiBinder<Widget, ChooseSourcePage> {
    }

    private ImportModel model;
    private final EventBus eventBus;

    @UiField
    TextArea textArea;

    public ChooseSourcePage(ImportModel model, EventBus eventBus) {
        this.model = model;
        this.eventBus = eventBus;
        initWidget(uiBinder.createAndBindUi(this));
        textArea.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                fireStateChanged();
            }
        });
    }

    @Override
    public void fireStateChanged() {

        // validate
        boolean valid = false;
        PastedTable pastedTable = null;
        try {
            pastedTable = new PastedTable(textArea.getValue());
            valid = !pastedTable.getRows().isEmpty();
            if (valid) {
                boolean isFirstColumnHeaderBlank = Strings.isNullOrEmpty(pastedTable.getColumnHeader(0));
                if (isFirstColumnHeaderBlank) { // first row may be occupied by attribute group name, so we just cut it
                    valid = false;
                    String source = textArea.getValue().substring(textArea.getValue().indexOf("\n") + 1);
                    pastedTable = new PastedTable(source);
                    valid = !pastedTable.getRows().isEmpty();
                }
            }
        } catch (Exception e) {
            // ignore : text is not valid
        }

        if (valid) {
            pastedTable.guessColumnsType(JsConverterFactory.get());
            model.setSource(pastedTable);
            eventBus.fireEvent(new PageChangedEvent(true, ""));
        } else {
            eventBus.fireEvent(new PageChangedEvent(false, errorMessage(pastedTable)));
        }
    }

    private String errorMessage(PastedTable pastedTable) {
        if (pastedTable != null && pastedTable.getFirstInvalidRow() > 0) {
            return I18N.MESSAGES.pleaseProvideCommaSeparatedText(pastedTable.getFirstInvalidRow());
        }
        return I18N.CONSTANTS.pleaseProvideCommaSeparatedText();
    }

    @UiHandler("textArea")
    public void onTextChanged(ChangeEvent event) {
        fireStateChanged();
    }

    @Override
    public boolean isValid() {
        if (!Strings.isNullOrEmpty(textArea.getValue())) {
            try {
                final PastedTable pastedTable = new PastedTable(textArea.getValue());
                return !pastedTable.getRows().isEmpty();
            } catch (Exception e) {
                // ignore : text is not valid
            }
        }
        return false;
    }

    @Override
    public boolean hasNextStep() {
        return false;
    }

    @Override
    public boolean hasPreviousStep() {
        return false;
    }

    @Override
    public void start() {

    }

    @Override
    public void nextStep() {
    }

    @Override
    public void previousStep() {
    }
}
