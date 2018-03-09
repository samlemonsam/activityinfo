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
package org.activityinfo.model.formula.simple;

import com.google.common.collect.ImmutableList;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.Formulas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple list of conditions that evaluates to a boolean expression
 */
public class SimpleConditionList {


    private Criteria criteria;
    private List<SimpleCondition> conditions;

    public SimpleConditionList() {
        this.criteria = Criteria.ALL_TRUE;
        this.conditions = Collections.emptyList();
    }

    public SimpleConditionList(SimpleCondition condition) {
        this.criteria = Criteria.ALL_TRUE;
        this.conditions = ImmutableList.of(condition);
    }

    public SimpleConditionList(Criteria criteria, SimpleCondition... conditions) {
        this.criteria = criteria;
        this.conditions = ImmutableList.copyOf(conditions);
    }

    public SimpleConditionList(Criteria critera, List<SimpleCondition> conditions) {
        this.criteria = critera;
        this.conditions = ImmutableList.copyOf(conditions);
    }

    public List<SimpleCondition> getConditions() {
        return conditions;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    public FormulaNode toFormula() {
        List<FormulaNode> conditions = new ArrayList<>();
        for (SimpleCondition condition : this.conditions) {
            conditions.add(condition.toFormula());
        }

        if(criteria == Criteria.ALL_TRUE) {
            return Formulas.allTrue(conditions);
        } else {
            return Formulas.anyTrue(conditions);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleConditionList that = (SimpleConditionList) o;
        if(!conditions.equals(that.conditions)) {
            return false;
        }

        if(conditions.size() > 1 && criteria != that.criteria) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = criteria.hashCode();
        result = 31 * result + conditions.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SimpleConditionList{" +
                "criteria=" + criteria +
                ", conditions=" + conditions +
                '}';
    }
}
