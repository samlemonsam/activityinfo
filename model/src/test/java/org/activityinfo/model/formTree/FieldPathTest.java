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
package org.activityinfo.model.formTree;

import org.activityinfo.model.resource.ResourceId;
import org.junit.Test;

import static org.activityinfo.model.formula.FormulaParser.parse;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class FieldPathTest  {

    private static final ResourceId A = ResourceId.valueOf("A");
    private static final ResourceId B = ResourceId.valueOf("B");
    private static final ResourceId C = ResourceId.valueOf("C");
    private static final ResourceId D = ResourceId.valueOf("D");

    @Test
    public void testToExpr() {
        assertThat(new FieldPath(A).toExpr(), equalTo(parse("A")));
        assertThat(new FieldPath(A, B).toExpr(), equalTo(parse("A.B")));
        assertThat(new FieldPath(A, B, C).toExpr(), equalTo(parse("A.B.C")));
        assertThat(new FieldPath(A, B, C, D).toExpr(), equalTo(parse("A.B.C.D")));
    }
}