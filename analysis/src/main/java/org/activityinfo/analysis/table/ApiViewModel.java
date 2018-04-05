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

import com.google.gwt.http.client.URL;
import org.activityinfo.json.Json;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.QueryModel;

import java.util.List;

public class ApiViewModel {

    private final EffectiveTableModel tableModel;

    public ApiViewModel(EffectiveTableModel tableModel) {
        this.tableModel = tableModel;
    }

    public String getSchemaUrl() {
        return formBaseUrl() + "/schema";
    }

    public String getQueryGetUrl() {
        StringBuilder url = new StringBuilder();
        url.append(formBaseUrl());
        url.append("/query/rows?");

        boolean ampersandNeeded = false;
        for (EffectiveTableColumn tableColumn : tableModel.getColumns()) {
            List<ColumnModel> columns = tableColumn.getQueryModel();
            if(columns.size() == 1) {
                appendQueryString(url, ampersandNeeded, tableColumn.getLabel(), columns.get(0));
                ampersandNeeded = true;
            } else if(columns.size() > 1) {
                for (int i = 0; i < columns.size(); i++) {
                    appendQueryString(url, ampersandNeeded, tableColumn.getLabel() + "." + i, columns.get(0));
                }
                ampersandNeeded = true;
            }
        }

        return url.toString();
    }

    private void appendQueryString(StringBuilder url, boolean ampersandNeeded, String queryName, ColumnModel column) {
        if(ampersandNeeded) {
            url.append("&");
        }
        url.append(URL.encodeQueryString(queryName));
        url.append("=");
        url.append(URL.encodeQueryString(column.getFormulaAsString()));
    }

    public String getQueryPostUrl() {
        return "/resources/query/rows";
    }

    public String getQueryPostBody() {

        QueryModel queryModel = new QueryModel(tableModel.getFormId());

        for (EffectiveTableColumn tableColumn : tableModel.getColumns()) {
            List<ColumnModel> columns = tableColumn.getQueryModel();
            if(columns.size() == 1) {
                ColumnModel columnModel = new ColumnModel();
                columnModel.setId(tableColumn.getLabel());
                columnModel.setFormula(tableColumn.getFormulaString());
                queryModel.addColumn(columnModel);
            } else if(columns.size() > 1) {
                for (int i = 0; i < columns.size(); i++) {
                    ColumnModel columnModel = new ColumnModel();
                    columnModel.setId(tableColumn.getLabel() + "." + i);
                    columnModel.setFormula(tableColumn.getFormulaString());
                    queryModel.addColumn(columnModel);
                }
            }
        }

        return Json.stringify(queryModel.toJson(), 2);
    }

    private String formBaseUrl() {
        return "/resources/form/" + tableModel.getFormId().asString();
    }

}
