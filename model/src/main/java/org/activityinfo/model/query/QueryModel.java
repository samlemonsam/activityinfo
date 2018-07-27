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
package org.activityinfo.model.query;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.FormulaParser;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.resource.ResourceId;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;

import java.util.List;

import static org.activityinfo.json.Json.createObject;

/**
 * Describes a Table to be constructed from a
 * FormTree.
 */
public class QueryModel implements JsonSerializable {

    private final List<RowSource> rowSources = Lists.newArrayList();
    private final List<ColumnModel> columns = Lists.newArrayList();

    private List<SortModel> sortModels = Lists.newArrayList();
    private FormulaNode filter;

    public QueryModel() {
    }

    /**
     * Creates a new TableModel using the given {@code formId} as the
     * root FormClassId
     */
    public QueryModel(ResourceId formId) {
        rowSources.add(new RowSource(formId));
    }

    public List<SortModel> getSortModels() {
        return sortModels;
    }

    public void setSortModels(List<SortModel> sortModels) {
        this.sortModels = sortModels;
    }

    public void addSortModel(SortModel sortModel) {
        this.sortModels.add(sortModel);
    }

    public List<RowSource> getRowSources() {
        return rowSources;
    }

    public List<ColumnModel> getColumns() {
        return columns;
    }
    
    public void addRowSource(ResourceId formId) {
        rowSources.add(new RowSource(formId));
    }

    /**
     * Adds a {@code ColumnModel} to this {@code TableModel} using the given
     * field's code or label as the column's id and value expression.
     */
    public ColumnModel selectField(String codeOrLabel) {
        ColumnModel column = new ColumnModel();
        column.setId(codeOrLabel);
        column.setFormula("[" + codeOrLabel + "]");
        columns.add(column);
        return column;
    }

    public ColumnModel selectExpr(String expression) {
        ColumnModel column = new ColumnModel();
        column.setId("_expr" + (columns.size()+1));
        column.setFormula(expression);
        columns.add(column);
        return column;
    }

    public ColumnModel selectExpr(FormulaNode expression) {
        ColumnModel column = new ColumnModel();
        column.setId("_expr" + (columns.size() + 1));
        column.setFormula(expression);
        columns.add(column);
        return column;
    }

    public ColumnModel selectField(FieldPath path) {
        ColumnModel column = new ColumnModel();
        column.setId(path.getLeafId().asString());
        column.setFormula(path);
        columns.add(column);
        return column;
    }

    /**
     * Adds a {@code ColumnModel} to this {@code TableModel} using the given
     * field's id as the column's id and value expression.
     */
    public ColumnModel selectField(ResourceId fieldId) {
        ColumnModel column = new ColumnModel();
        column.setId(fieldId.asString());
        column.setFormula(fieldId.asString());
        columns.add(column);
        return column;
    }

    public FormulaNode getFilter() {
        return filter;
    }

    @JsonProperty("filter")
    public String getFilterAsString() {
        if(filter == null) {
            return null;
        } else {
            return filter.toString();
        }
    }

    @JsonSetter
    public void setFilter(String filterExpression) {
        if(filterExpression == null) {
            this.filter = null;
        } else {
            this.filter = FormulaParser.parse(filterExpression);
        }
    }

    public void setFilter(FormulaNode filter) {
        this.filter = filter;
    }

    public void setFilter(Optional<FormulaNode> filter) {
        if(filter.isPresent()) {
            this.filter = filter.get();
        } else {
            this.filter = null;
        }
    }

    public QueryModel addColumn(ColumnModel criteriaColumn) {
        columns.add(criteriaColumn);
        return this;
    }

    public void addColumns(List<ColumnModel> requiredColumns) {
        columns.addAll(requiredColumns);
    }

    /**
     * Adds the {@code ResourceId} as a string column to the table model with
     * the given column id
     */
    public ColumnModel selectRecordId() {
        ColumnModel columnModel = new ColumnModel();
        columnModel.setFormula(new SymbolNode(ColumnModel.RECORD_ID_SYMBOL));
        columns.add(columnModel);
        return columnModel;
    }

    public ColumnModel selectClassId() {
        ColumnModel columnModel = new ColumnModel();
        columnModel.setFormula(new SymbolNode(ColumnModel.FORM_ID_SYMBOL));
        columns.add(columnModel);
        return columnModel;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SELECT ");
        boolean needsComma = false;
        for(ColumnModel column : columns) {
            if(needsComma) {
                sb.append(", ");
            }
            sb.append("(").append(column.getFormula()).append(") as ").append(column.getId());
            needsComma = true;
        }
        return sb.toString();
    }
    
    public String toJsonString() {
        return toJson().toJson();
    }

    public static QueryModel fromJson(JsonValue jsonObject) {
        QueryModel queryModel = new QueryModel();
        JsonValue rowSources = jsonObject.get("rowSources");
        for (JsonValue rowSource : rowSources.values()) {
            queryModel.getRowSources().add(RowSource.fromJson(rowSource));
        }

        for (JsonValue column : jsonObject.get("columns").values()) {
            queryModel.getColumns().add(ColumnModel.fromJson(column));
        }

        String filter = jsonObject.getString("filter");
        if (!Strings.isNullOrEmpty(filter)) {
            queryModel.setFilter(filter);
        }

        for (JsonValue sort : jsonObject.get("sort").values()) {
            queryModel.addSortModel(SortModel.fromJson(sort));
        }

        return queryModel;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = createObject();
        object.put("rowSources", Json.toJson(rowSources));
        object.put("columns", Json.toJson(columns));

        if(filter != null) {
            object.put("filter", filter.asExpression());
        }

        if(!sortModels.isEmpty()) {
            object.put("sort", Json.toJson(sortModels));
        }

        return object;
    }

}

