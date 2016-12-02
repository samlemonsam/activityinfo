package org.activityinfo.model.query;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.expr.ExprValue;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;

import java.util.List;

/**
 * Describes a Table to be constructed from a
 * FormTree.
 */
public class QueryModel {

    private final List<RowSource> rowSources = Lists.newArrayList();
    private final List<ColumnModel> columns = Lists.newArrayList();

    private ExprNode filter;


    public QueryModel() {
    }

    /**
     * Creates a new TableModel using the given {@code formId} as the
     * root FormClassId
     */
    public QueryModel(ResourceId formId) {
        rowSources.add(new RowSource(formId));
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
        column.setExpression("[" + codeOrLabel + "]");
        columns.add(column);
        return column;
    }

    public ColumnModel selectExpr(String expression) {
        ColumnModel column = new ColumnModel();
        column.setId("_expr" + (columns.size()+1));
        column.setExpression(expression);
        columns.add(column);
        return column;
    }

    public ColumnModel selectExpr(ExprNode expression) {
        ColumnModel column = new ColumnModel();
        column.setId("_expr" + (columns.size() + 1));
        column.setExpression(expression);
        columns.add(column);
        return column;
    }

    public ColumnModel selectField(FieldPath path) {
        ColumnModel column = new ColumnModel();
        column.setId(path.getLeafId().asString());
        column.setExpression(path);
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
        column.setExpression(fieldId.asString());
        columns.add(column);
        return column;
    }

    public ExprNode getFilter() {
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
            this.filter = ExprParser.parse(filterExpression);
        }
    }

    public void setFilter(ExprValue filter) {
        if(filter == null) {
            this.filter = null;
        } else {
            setFilter(filter.getExpression());
        }
    }
    
    public void setFilter(ExprNode filter) {
        this.filter = filter;
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
    public ColumnModel selectResourceId() {
        ColumnModel columnModel = new ColumnModel();
        columnModel.setExpression(new SymbolExpr(ColumnModel.ID_SYMBOL));
        columns.add(columnModel);
        return columnModel;
    }

    public ColumnModel selectClassId() {
        ColumnModel columnModel = new ColumnModel();
        columnModel.setExpression(new SymbolExpr(ColumnModel.CLASS_SYMBOL));
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
            sb.append("(").append(column.getExpression()).append(") as ").append(column.getId());
            needsComma = true;
        }
        return sb.toString();
    }
    
    public String toJsonString() {
        return toJsonElement().toString();
    }

    public static QueryModel fromJson(String json) {
        QueryModel queryModel = new QueryModel();

        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();

        JsonArray rowSources = jsonObject.getAsJsonArray("rowSources");
        for (JsonElement rowSource : rowSources) {
            queryModel.getRowSources().add(RowSource.fromJson(rowSource.getAsJsonObject()));
        }

        for (JsonElement column : jsonObject.getAsJsonArray("columns")) {
            queryModel.getColumns().add(ColumnModel.fromJson(column.getAsJsonObject()));
        }

        if(jsonObject.has("filter")) {
            String filter = jsonObject.get("filter").getAsString();
            if (!Strings.isNullOrEmpty(filter)) {
                queryModel.setFilter(filter);
            }
        }
        return queryModel;
    }

    public JsonObject toJsonElement() {

        JsonArray sourcesArray = new JsonArray();
        for (RowSource rowSource : rowSources) {
            sourcesArray.add(rowSource.toJsonElement());
        }

        JsonArray columnsArray = new JsonArray();
        for (ColumnModel column : columns) {
            columnsArray.add(column.toJsonElement());
        }

        JsonObject object = new JsonObject();
        object.add("rowSources", sourcesArray);
        object.add("columns", columnsArray);
        return object;
    }
}

