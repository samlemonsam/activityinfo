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
package org.activityinfo.i18n.tools.po;

import com.google.common.base.Strings;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;

public class PoDefinition {
    private String form;

    @JsonSerialize(include= JsonSerialize.Inclusion.NON_DEFAULT)
    private double fuzzy;
    
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private DateTime updated;

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public double getFuzzy() {
        return fuzzy;
    }

    public void setFuzzy(double fuzzy) {
        this.fuzzy = fuzzy;
    }

    public DateTime getUpdated() {
        return updated;
    }

    public void setUpdated(DateTime updated) {
        this.updated = updated;
    }
    
    @JsonSetter
    public void setUpdated(String updated) {
        if(Strings.isNullOrEmpty(updated)) {
            this.updated = null;
        } else {
            this.updated = PoTerm.DATE_TIME_FORMAT.parseDateTime(updated);
        }
    }
}
