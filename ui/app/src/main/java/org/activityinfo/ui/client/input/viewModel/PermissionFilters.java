package org.activityinfo.ui.client.input.viewModel;

import com.gargoylesoftware.htmlunit.javascript.host.Symbol;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.analysis.FieldReference;
import org.activityinfo.analysis.FormulaValidator;
import org.activityinfo.model.expr.*;
import org.activityinfo.model.expr.functions.AndFunction;
import org.activityinfo.model.form.FormOperation;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;

import java.util.*;

/**
 * Calculates the filters that apply to individual fields based on form-level permissions.
 */
public class PermissionFilters {

    private final Map<ResourceId, ExprNode> fieldFilters = new HashMap<>();


    public PermissionFilters(FormTree formTree) {
        this(formTree, formTree.getRootMetadata().getPermissions());
    }

    @VisibleForTesting
    PermissionFilters(FormTree formTree, FormPermissions permissions) {

        /*
         * Create a set of independent boolean permission criteria.
         */
        Set<ExprNode> criteria = new HashSet<>();
        if(permissions.hasVisibilityFilter()) {
            criteria.addAll(parsePermission(permissions.getViewFilter()));
        }
        if(permissions.isFiltered(FormOperation.EDIT_RECORD)) {
            criteria.addAll(parsePermission(permissions.getFilter(FormOperation.EDIT_RECORD)));
        }

        /*
         * Now map each of the criteria to a field, if possible
         */
        Multimap<ResourceId, ExprNode> fieldCriteria = HashMultimap.create();
        for (ExprNode criterium : criteria) {
            FormulaValidator validator = new FormulaValidator(formTree);
            validator.validate(criterium);
            if(validator.isValid()) {
                Optional<ResourceId> rootField = findUniqueFieldReference(validator.getReferences());
                if(rootField.isPresent()) {
                    fieldCriteria.put(rootField.get(), criterium);
                }
            }
        }

        /*
         * Finally combine all the separate filters into an expression per field.
         */
        for (ResourceId fieldId : fieldCriteria.keySet()) {
            fieldFilters.put(fieldId, Exprs.allTrue(fieldCriteria.get(fieldId)));
        }
    }

    private List<ExprNode> parsePermission(String filter) {
        ExprNode exprNode = ExprParser.parse(filter);
        return Exprs.findBinaryTree(exprNode, AndFunction.INSTANCE);
    }

    private Optional<ResourceId> findUniqueFieldReference(List<FieldReference> references) {
        Set<ResourceId> rootFields = new HashSet<>();

        for (FieldReference reference : references) {
            switch (reference.getMatch().getType()) {
                case ID:
                    return Optional.absent();
                case CLASS:
                    return Optional.absent();
                case FIELD:
                    rootFields.add(reference.getMatch().getFieldNode().getPath().getRoot());
                    break;
            }
        }
        if(rootFields.size() == 1) {
            return Optional.of(rootFields.iterator().next());
        } else {
            return Optional.absent();
        }
    }

    public boolean isFiltered(ResourceId fieldId) {
        return fieldFilters.containsKey(fieldId);
    }

    /**
     * Returns a filter for records referenced by the given field.
     */
    public Optional<ExprNode> getReferenceBaseFilter(ResourceId fieldId) {
        ExprNode filter = fieldFilters.get(fieldId);
        if(filter == null) {
            return Optional.absent();
        }

        SymbolExpr fieldExpr = new SymbolExpr(fieldId);

        return Optional.of(filter.transform(x -> {
            if(x.equals(fieldExpr)) {
                return new SymbolExpr(ColumnModel.ID_SYMBOL);
            } else {
                return x;
            }
        }));
    }
}
