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
package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.analysis.ImmutableTableColumn;
import org.activityinfo.model.analysis.TableColumn;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.analysis.pivot.ImmutableMeasureModel;
import org.activityinfo.model.analysis.pivot.MeasureModel;
import org.activityinfo.ui.client.icons.IconBundle;

import java.util.Optional;

public class IdNode extends MeasureTreeNode {

    private FormClass formClass;
    private FormulaNode formula;

    public IdNode(FormClass formClass) {
        this.formClass = formClass;
        this.formula = new SymbolNode(ColumnModel.RECORD_ID_SYMBOL);
    }

    @Override
    public String getId() {
        return ColumnModel.RECORD_ID_SYMBOL;
    }

    @Override
    public String getLabel() {
        return "Record ID";
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.INSTANCE.textField();
    }

    @Override
    public MeasureModel newMeasure() {
        return ImmutableMeasureModel.builder()
                .label(getLabel())
                .formId(formClass.getId())
                .formula(formula.asExpression())
                .build();
    }

    @Override
    public Optional<TableColumn> newTableColumn() {
        return Optional.of(ImmutableTableColumn.builder()
            .label(getLabel())
            .formula(formula.asExpression())
            .build());
    }
}
