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
package org.activityinfo.store.testing;

import org.activityinfo.model.form.FormInstance;

import java.util.ArrayList;
import java.util.List;

public class LazyRecordList {
    private final RecordGenerator generator;
    private int count;

    private List<FormInstance> records;

    public LazyRecordList(RecordGenerator generator, int count) {
        this.generator = generator;
        this.count = count;
    }

    public List<FormInstance> get() {
        if(records == null) {
            records = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                records.add(generator.get());
            }
        }
        return records;
    }
}
