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
