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

import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.type.time.LocalDate;

import java.util.Date;

public class DateFormat extends SimpleColumnFormat<Date> {

    protected DateFormat(String id, FormulaNode formula) {
        super(id, formula);
    }

    @Override
    public ColumnRenderer<Date> createRenderer() {
        return new DateRenderer(getId());
    }

    public ColumnRenderer<LocalDate> createLocalDateRenderer() {
        return new LocalDateRenderer(getId());
    }

    @Override
    public <T> T accept(EffectiveTableColumn columnModel, TableColumnVisitor<T> visitor) {
        return visitor.visitDateColumn(columnModel, this);
    }
}
