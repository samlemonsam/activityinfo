package org.activityinfo.server.command;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.reports.model.DateRange;
import org.activityinfo.model.expr.*;
import org.activityinfo.model.expr.functions.GreaterOrEqualFunction;
import org.activityinfo.model.expr.functions.LessOrEqualFunction;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.time.LocalDate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.activityinfo.model.expr.Exprs.anyTrue;
import static org.activityinfo.model.expr.Exprs.idConstant;
import static org.activityinfo.model.expr.Exprs.symbol;

public class QueryFilter {

    private Filter filter;
    private Multimap<String,String> attributeFilters;
    private Logger LOGGER;

    public QueryFilter(Filter filter, Multimap<String,String> attributeFilters) {
        this.filter = filter;
        this.attributeFilters = attributeFilters;
    }

    public QueryFilter(Filter filter, Multimap<String,String> attributeFilters, Logger LOGGER) {
        this(filter,attributeFilters);
        this.LOGGER = LOGGER;
    }

    public ExprNode composeFilter(FormTree formTree) {
        List<ExprNode> conditions = Lists.newArrayList();
        conditions.addAll(filterExpr(siteIdField(formTree), CuidAdapter.SITE_DOMAIN, DimensionType.Site));
        conditions.addAll(filterExpr("partner", CuidAdapter.PARTNER_DOMAIN, DimensionType.Partner));
        conditions.addAll(filterExpr("project", CuidAdapter.PROJECT_DOMAIN, DimensionType.Project));
        conditions.addAll(filterExpr("location", CuidAdapter.LOCATION_DOMAIN, DimensionType.Location));
        conditions.addAll(adminFilter(formTree));
        conditions.addAll(attributeFilters());
        conditions.addAll(dateFilter("date1", filter.getStartDateRange()));
        conditions.addAll(dateFilter("date2", filter.getEndDateRange()));

        if(conditions.size() > 0) {
            ExprNode filterExpr = Exprs.allTrue(conditions);
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

    public ExprNode composeTargetFilter() {
        List<ExprNode> conditions = Lists.newArrayList();
        conditions.addAll(filterExpr("partner", CuidAdapter.PARTNER_DOMAIN, DimensionType.Partner));
        conditions.addAll(filterExpr("project", CuidAdapter.PROJECT_DOMAIN, DimensionType.Project));

        conditions.addAll(dateFilter("fromDate", filter.getStartDateRange()));
        conditions.addAll(dateFilter("toDate", filter.getEndDateRange()));

        if(conditions.isEmpty()) {
            return null;
        }

        ExprNode filterExpr = Exprs.allTrue(conditions);
        if (LOGGER != null)
            LOGGER.fine("Filter: " + filterExpr);

        return filterExpr;
    }


    private Set<ExprNode> adminFilter(FormTree formTree) {
        if (this.filter.isRestricted(DimensionType.AdminLevel)) {

            List<ExprNode> conditions = Lists.newArrayList();

            // we don't know which adminlevel this belongs to so we have construct a giant OR statement
            List<ExprNode> adminIdExprs = findAdminIdExprs(formTree);

            for(ExprNode adminIdExpr : adminIdExprs) {
                for (Integer adminEntityId : this.filter.getRestrictions(DimensionType.AdminLevel)) {
                    conditions.add(Exprs.equals(adminIdExpr, idConstant(CuidAdapter.entity(adminEntityId))));
                }
            }
            return singleton(anyTrue(conditions));

        } else {
            return emptySet();
        }
    }

    private List<ExprNode> findAdminIdExprs(FormTree formTree) {
        List<ExprNode> expressions = Lists.newArrayList();

        for (FormMetadata form : formTree.getForms()) {
            if (form.getId().getDomain() == CuidAdapter.ADMIN_LEVEL_DOMAIN) {
                expressions.add(new CompoundExpr(form.getId(), ColumnModel.ID_SYMBOL));
            }
        }
        return expressions;
    }

    private List<ExprNode> attributeFilters() {

        List<ExprNode> conditions = Lists.newArrayList();

        for (String field : attributeFilters.keySet()) {
            List<ExprNode> valueConditions = Lists.newArrayList();
            for (String value : attributeFilters.get(field)) {
                valueConditions.add(new CompoundExpr(new SymbolExpr(field), new SymbolExpr(value)));
            }
            conditions.add(Exprs.anyTrue(valueConditions));
        }
        return conditions;
    }

    private List<ExprNode> filterExpr(String fieldName, char domain, DimensionType type) {
        Set<Integer> ids = this.filter.getRestrictions(type);
        if(ids.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<ExprNode> conditions = Lists.newArrayList();

            for (Integer id : ids) {
                conditions.add(Exprs.equals(symbol(fieldName), idConstant(CuidAdapter.cuid(domain, id))));
            }

            return singletonList(Exprs.anyTrue(conditions));
        }
    }


    private Collection<FunctionCallNode> dateFilter(String dateField, DateRange range) {

        SymbolExpr dateExpr = new SymbolExpr(dateField);
        List<FunctionCallNode> conditions = Lists.newArrayList();
        if(range != null) {
            if (range.getMinLocalDate() != null) {
                conditions.add(new FunctionCallNode(GreaterOrEqualFunction.INSTANCE, dateExpr, new ConstantExpr(new LocalDate(range.getMinDate()))));
            }
            if (range.getMaxLocalDate() != null) {
                conditions.add(new FunctionCallNode(LessOrEqualFunction.INSTANCE, dateExpr, new ConstantExpr(new LocalDate(range.getMaxDate()))));
            }
        }
        return conditions;
    }
}
