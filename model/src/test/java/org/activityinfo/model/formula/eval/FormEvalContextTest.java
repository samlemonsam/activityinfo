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
package org.activityinfo.model.formula.eval;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormEvalContext;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ErrorValue;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.junit.Test;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

public class FormEvalContextTest {

    @Test
    public void circularRefs() {

        FormField a = new FormField(ResourceId.generateId());
        a.setCode("A");
        a.setType(new CalculatedFieldType("B+1"));
        a.setLabel("A");

        FormField b = new FormField(ResourceId.generateId());
        b.setCode("B");
        b.setType(new CalculatedFieldType("A/50"));
        b.setLabel("B");

        FormClass formClass = new FormClass(ResourceId.generateId());
        formClass.addElement(a);
        formClass.addElement(b);

        FormEvalContext context = new FormEvalContext(formClass);
        context.setInstance(new FormInstance(ResourceId.generateSubmissionId(formClass), formClass.getId()));

        assertThat(context.getFieldValue(a.getId()), instanceOf(ErrorValue.class));

    }

}