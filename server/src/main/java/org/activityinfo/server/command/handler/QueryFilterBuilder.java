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
package org.activityinfo.server.command.handler;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.reports.model.DateRange;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formula.*;
import org.activityinfo.model.formula.functions.GreaterOrEqualFunction;
import org.activityinfo.model.formula.functions.LessOrEqualFunction;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.time.LocalDate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static java.util.Collections.*;
import static org.activityinfo.model.formula.Formulas.*;

public class QueryFilterBuilder {

    private static final Logger LOGGER = Logger.getLogger(QueryFilterBuilder.class.getName());

    private Filter filter;
    private AttributeFilterMap attributeFilters;

    public QueryFilterBuilder(Filter filter, AttributeFilterMap attributeFilters) {
        this.filter = filter;
        this.attributeFilters = attributeFilters;
    }

    public FormulaNode composeFilter(FormTree formTree) {
        List<FormulaNode> conditions = Lists.newArrayList();
        conditions.addAll(filterExpr(siteIdField(formTree), CuidAdapter.SITE_DOMAIN, DimensionType.Site));
        conditions.addAll(filterExpr("partner", CuidAdapter.PARTNER_DOMAIN, DimensionType.Partner));
        conditions.addAll(filterExpr("project", CuidAdapter.PROJECT_DOMAIN, DimensionType.Project));
        conditions.addAll(filterExpr("location", CuidAdapter.LOCATION_DOMAIN, DimensionType.Location));
        conditions.addAll(adminFilter(formTree));
        conditions.addAll(attributeFilters());
        conditions.addAll(dateFilter("date1", filter.getStartDateRange()));
        conditions.addAll(dateFilter("date2", filter.getEndDateRange()));

        if(conditions.size() > 0) {
            FormulaNode filterExpr = Formulas.allTrue(conditions);
            if (LOGGER != null)
                LOGGER.fine("Filter: " + filterExpr);

            return filterExpr;

        } else {
            return null;
        }
    }

    private String siteIdField(FormTree formTree) {
        Preconditions.checkNotNull(formTree, "formTree");
        Preconditions.checkNotNull(formTree.getRootFormClass(), "formTree.rootFormClass");

        ResourceId rootFormClassId = formTree.getRootFormId();
        if(rootFormClassId.getDomain() == CuidAdapter.ACTIVITY_DOMAIN) {
            // Root form class is the site, we need to compare against the ID
            return ColumnModel.ID_SYMBOL;
        } else {
            // ROot form class is monhtly report, filter against the site id
            return CuidAdapter.field(rootFormClassId, CuidAdapter.SITE_FIELD).asString();
        }
    }

    public FormulaNode composeTargetFilter() {
        List<FormulaNode> conditions = Lists.newArrayList();
        conditions.addAll(filterExpr("partner", CuidAdapter.PARTNER_DOMAIN, DimensionType.Partner));
        conditions.addAll(filterExpr("project", CuidAdapter.PROJECT_DOMAIN, DimensionType.Project));

        conditions.addAll(dateFilter("fromDate", filter.getStartDateRange()));
        conditions.addAll(dateFilter("toDate", filter.getEndDateRange()));

        if(conditions.isEmpty()) {
            return null;
        }

        FormulaNode filterExpr = Formulas.allTrue(conditions);
        if (LOGGER != null)
            LOGGER.fine("Filter: " + filterExpr);

        return filterExpr;
    }


    private Set<FormulaNode> adminFilter(FormTree formTree) {
        if (this.filter.isRestricted(DimensionType.AdminLevel)) {

            List<FormulaNode> conditions = Lists.newArrayList();

            // we don't know which adminlevel this belongs to so we have construct a giant OR statement
            List<FormulaNode> adminIdExprs = findAdminIdExprs(formTree);

            for(FormulaNode adminIdExpr : adminIdExprs) {
                for (Integer adminEntityId : this.filter.getRestrictions(DimensionType.AdminLevel)) {
                    conditions.add(Formulas.equals(adminIdExpr, idConstant(CuidAdapter.entity(adminEntityId))));
                }
            }
            return singleton(anyTrue(conditions));

        } else {
            return emptySet();
        }
    }

    private List<FormulaNode> findAdminIdExprs(FormTree formTree) {
        List<FormulaNode> expressions = Lists.newArrayList();

        for (FormMetadata form : formTree.getForms()) {
            if (form.getId().getDomain() == CuidAdapter.ADMIN_LEVEL_DOMAIN) {
                expressions.add(new CompoundExpr(form.getId(), ColumnModel.ID_SYMBOL));
            }
        }
        return expressions;
    }

    private List<FormulaNode> attributeFilters() {

        List<FormulaNode> conditions = Lists.newArrayList();

        for (String field : attributeFilters.getFilteredFieldNames()) {
            List<FormulaNode> valueConditions = Lists.newArrayList();
            for (String value : attributeFilters.getFilteredValues(field)) {
                valueConditions.add(new CompoundExpr(new SymbolNode(field), new SymbolNode(value)));
            }
            conditions.add(Formulas.anyTrue(valueConditions));
        }
        return conditions;
    }

    private List<FormulaNode> filterExpr(String fieldName, char domain, DimensionType type) {
        Set<Integer> ids = this.filter.getRestrictions(type);
        if(ids.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<FormulaNode> conditions = Lists.newArrayList();

            for (Integer id : ids) {
                conditions.add(Formulas.equals(symbol(fieldName), idConstant(CuidAdapter.cuid(domain, id))));
            }

            return singletonList(Formulas.anyTrue(conditions));
        }
    }


    private Collection<FunctionCallNode> dateFilter(String dateField, DateRange range) {

        SymbolNode dateExpr = new SymbolNode(dateField);
        List<FunctionCallNode> conditions = Lists.newArrayList();
        if(range != null) {
            if (range.getMinLocalDate() != null) {
                conditions.add(new FunctionCallNode(GreaterOrEqualFunction.INSTANCE, dateExpr, new ConstantNode(new LocalDate(range.getMinDate()))));
            }
            if (range.getMaxLocalDate() != null) {
                conditions.add(new FunctionCallNode(LessOrEqualFunction.INSTANCE, dateExpr, new ConstantNode(new LocalDate(range.getMaxDate()))));
            }
        }
        return conditions;
    }
}
