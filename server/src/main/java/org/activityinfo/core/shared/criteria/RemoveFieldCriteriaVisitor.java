package org.activityinfo.core.shared.criteria;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author yuriyz on 12/28/2015.
 */
public class RemoveFieldCriteriaVisitor extends CriteriaVisitor {

    public void visitIntersection(CriteriaIntersection intersection) {
        List<FieldCriteria> toRemove = visitIterable(intersection);
        intersection.getElements().removeAll(toRemove);
    }

    public void visitUnion(CriteriaUnion unionCriteria) {
        List<FieldCriteria> toRemove = visitIterable(unionCriteria);
        unionCriteria.getElements().removeAll(toRemove);
    }

    private List<FieldCriteria> visitIterable(Iterable<Criteria> intersection) {
        List<FieldCriteria> toRemove = Lists.newArrayList();
        for (Criteria criteria : intersection) {
            if (criteria instanceof FieldCriteria) {
                toRemove.add((FieldCriteria) criteria);
            } else {
                criteria.accept(this);
            }
        }
        return toRemove;
    }

}
