package org.activityinfo.model.expr.eval;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;

/**
 * Describes a field matched to a symbol, along
 * with (optionally) a restriction on which value of the field to choose.
 */
public class SymbolBinding {
    private FormTree.Node field;
    private Predicate<FormTree.Node> childPredicate;

    SymbolBinding(FormTree.Node field, ResourceId formClassId) {
        this.field = field;
        this.childPredicate = new FormClassPredicate(formClassId);
    }

    public SymbolBinding(FormTree.Node field) {
        this.field = field;
        this.childPredicate = Predicates.alwaysTrue();
    }

    public FormTree.Node getField() {
        return field;
    }

    public Predicate<FormTree.Node> getChildPredicate() {
        return childPredicate;
    }

    @Override
    public String toString() {
        return field.toString();
    }


    private static class FormClassPredicate implements Predicate<FormTree.Node> {

        private final ResourceId formClassId;

        private FormClassPredicate(ResourceId formClassId) {
            this.formClassId = formClassId;
        }

        @Override
        public boolean apply(FormTree.Node input) {
            return input.getDefiningFormClass().getId().equals(formClassId);
        }
    }
}
