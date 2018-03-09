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
package org.activityinfo.test.pageobject.web.entry;

import cucumber.api.DataTable;
import gherkin.formatter.model.DataTableRow;

/**
 * @author yuriyz on 09/02/2015.
 */
public class ExcelConvertor {

    private ExcelConvertor() {
    }

    public static String asExcelData(DataTable dataTable) {
        String s = "";

        for (int i = 0; i < dataTable.getGherkinRows().size(); i++) {
            DataTableRow row = dataTable.getGherkinRows().get(i);

            for (int j = 0; j < row.getCells().size(); j++) {
                String cell = row.getCells().get(j);

                s = s + cell;

                boolean isLastCell = j == (row.getCells().size() - 1);
                if (!isLastCell) {
                    s += ";";
                }
            }
            boolean isLastRow = i == (dataTable.getGherkinRows().size() - 1);
            if (!isLastRow) {
                s = s + "\n";
            }

        }
        return s;
    }
}
