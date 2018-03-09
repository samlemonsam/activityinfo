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
package org.activityinfo.analysis.table;

import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.query.ColumnModel;

import java.util.Arrays;
import java.util.List;

public class GeoPointFormat implements ColumnFormat {

    private String columnId;
    private FormulaNode formula;

    public GeoPointFormat(String columnId, FormulaNode formula) {
        this.columnId = columnId;
        this.formula = formula;
    }

    public String getLatitudeId() {
        return columnId + ":lat";
    }

    public String getLongitudeId() {
        return columnId + ":lng";
    }

    private ColumnRenderer<Double> createRenderer(String id) {
        return new DoubleRenderer(id);
    }

    public ColumnRenderer<Double> createLatitudeRenderer() {
        return createRenderer(getLatitudeId());
    }

    public ColumnRenderer<Double> createLongitudeRenderer() {
        return createRenderer(getLongitudeId());
    }

    @Override
    public List<ColumnModel> getColumnModels() {

        ColumnModel latitudeModel = new ColumnModel();
        latitudeModel.setId(getLatitudeId());
        latitudeModel.setFormula(new CompoundExpr(formula, new SymbolNode("latitude")));

        ColumnModel longitudeModel = new ColumnModel();
        longitudeModel.setId(getLongitudeId());
        longitudeModel.setFormula(new CompoundExpr(formula, new SymbolNode("longitude")));

        return Arrays.asList(latitudeModel, longitudeModel);
    }

    @Override
    public <T> T accept(EffectiveTableColumn columnModel, TableColumnVisitor<T> visitor) {
        return visitor.visitGeoPointColumn(columnModel, this);
    }
}
