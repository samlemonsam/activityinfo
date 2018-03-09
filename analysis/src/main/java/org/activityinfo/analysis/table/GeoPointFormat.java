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
