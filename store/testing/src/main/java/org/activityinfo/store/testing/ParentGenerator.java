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

import com.google.common.base.Supplier;
import org.activityinfo.model.resource.ResourceId;

import java.util.Random;

public class ParentGenerator implements Supplier<ResourceId> {

    private final Random random = new Random(529523452L);
    private final TestForm parentForm;

    public ParentGenerator(TestForm parentForm) {
        this.parentForm = parentForm;
    }


    @Override
    public ResourceId get() {
        int index = random.nextInt(parentForm.getRecords().size());
        return parentForm.getRecords().get(index).getId();
    }
}
