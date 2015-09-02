package org.activityinfo.test.pageobject.web.entry;
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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import cucumber.api.DataTable;
import gherkin.formatter.model.DataTableRow;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.util.Pair;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.bootstrap.BsModal;
import org.activityinfo.test.pageobject.bootstrap.BsTable;

import java.util.List;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;

/**
 * @author yuriyz on 09/02/2015.
 */
public class ImportDialog {

    private final BsModal modal;

    public ImportDialog(BsModal modal) {
        this.modal = modal;
    }

    public ImportDialog enterExcelData(DataTable dataTable) {
        return enterExcelData(ExcelConvertor.asExcelData(dataTable));
    }

    public ImportDialog enterExcelData(String excelData) {
        FluentElement textArea = modal.form().getForm().find().textArea(withClass("form-control")).first();
        textArea.element().clear();
        textArea.sendKeys(excelData);
        return this;
    }

    public ImportDialog enterMapping(DataTable mappingTable) {
        List<Pair<String, String>> mapping = Lists.newArrayList();
        for (DataTableRow row : mappingTable.getGherkinRows()) {
            mapping.add(Pair.newPair(row.getCells().get(0), row.getCells().get(1)));
        }
        return enterMapping(mapping);
    }

    public ImportDialog enterMapping(List<Pair<String, String>> mapping) {
        for (Pair<String, String> pair : mapping) {
            String sourceName = pair.getFirst();
            String targetName = pair.getSecond();
            selectSourceColumn(sourceName);
            selectTargetName(targetName);
        }
        return this;
    }

    private void selectTargetName(String targetName) {
        FluentElement panel = modal.getWindowElement().find().div(withClass("panel-body")).first();
        Optional<FluentElement> radio = panel.find().label(withText(targetName)).ancestor().span(withClass("radio")).firstIfPresent();
        if (radio.isPresent()) {
            radio.get().clickWhenReady();
            return;
        }
        throw new AssertionError("Failed to find target column: " + targetName);
    }

    private void selectSourceColumn(String sourceName) {
        BsTable table = new BsTable(modal.getWindowElement(), BsTable.Type.DATA_GRID_TABLE);
        table.waitForCellByText(sourceName).select();
    }

    public static ImportDialog find(FluentElement container) {
        return new ImportDialog(BsModal.find(container, "gwt-PopupPanel"));
    }

    public ImportDialog clickNextButton() {
        modal.click(I18N.CONSTANTS.nextButton());
        return this;
    }

    public ImportDialog clickFinishButton() {
        modal.click(I18N.CONSTANTS.finish());
        return this;
    }

    public ImportDialog clickPreviousButton() {
        modal.click(I18N.CONSTANTS.previousButton());
        return this;
    }

    public ImportDialog clickCancelButton() {
        modal.click(I18N.CONSTANTS.cancel());
        return this;
    }

    public void waitUntilClosed() {
        modal.waitUntilClosed();
    }
}
