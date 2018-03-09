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
package org.activityinfo.ui.client.table;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import org.activityinfo.model.resource.ResourceId;

/**
 * Table view place for a specific form.
 */
public class TablePlace extends Place {

    private ResourceId formId;

    public TablePlace(ResourceId formId) {
        this.formId = formId;
    }

    public ResourceId getFormId() {
        return formId;
    }

    public static class Tokenizer implements PlaceTokenizer<TablePlace> {
        @Override
        public TablePlace getPlace(String token) {
            return new TablePlace(ResourceId.valueOf(token));
        }

        @Override
        public String getToken(TablePlace place) {
            return place.getFormId().asString();
        }
    }
}
