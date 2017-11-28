package org.activityinfo.model.formTree;


import org.activityinfo.model.resource.ResourceId;
import org.junit.Test;

import static org.activityinfo.model.expr.ExprParser.parse;
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