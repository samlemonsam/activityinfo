package org.activityinfo.model.expr.diagnostic;

import com.google.common.base.Joiner;
import org.activityinfo.model.form.FormField;

import java.util.List;

public class CircularReferenceException extends ExprException {

    public CircularReferenceException() {
    }

    public CircularReferenceException(String symbol) {
        super(symbol);
    }

    public CircularReferenceException(List<FormField> stack) {
        super("Circular reference: " + Joiner.on(" -> ").join(stack));
    }
}
