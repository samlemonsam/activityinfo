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
package org.activityinfo.api.tools;

import java.util.ArrayList;
import java.util.List;

public class ApiSectionModel {
    private final String tag;
    private String title;
    private List<OperationModel> operations = new ArrayList<>();

    public ApiSectionModel(String tag, String title, List<OperationModel> operations) {
        this.tag = tag;
        this.title = title;
        for (OperationModel operation : operations) {
            if(operation.getTags().contains(tag)) {
                this.operations.add(operation);
            }
        }
    }

    public String getTag() {
        return tag;
    }

    public String getTitle() {
        return title;
    }

    public List<OperationModel> getOperations() {
        return operations;
    }
}
