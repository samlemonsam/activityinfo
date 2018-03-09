package org.activityinfo.ui.client.input.viewModel;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.analysis.FieldReference;
import org.activityinfo.analysis.FormulaValidator;
import org.activityinfo.model.form.FormOperation;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.FormulaParser;
import org.activityinfo.model.formula.Formulas;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.formula.functions.AndFunction;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;

import java.util.*;

/**
 * Calculates the filters that apply to individual fields based on form-level permissions.
 */
public class PermissionFilters {

    private final Map<ResourceId, FormulaNode> fieldFilters = new HashMap<>();


    public PermissionFilters(FormTree formTree) {
        this(formTree, formTree.getRootMetadata().getPermissions());
    }

    @VisibleForTesting
    PermissionFilters(FormTree formTree, FormPermissions permissions) {

        /*
         * Create a set of independent boolean permission criteria.
         */
        Set<FormulaNode> criteria = new HashSet<>();
        if(permissions.hasVisibilityFilter()) {
            criteria.addAll(parsePermission(permissions.getViewFilter()));
        }
        if(permissions.isFiltered(FormOperation.EDIT_RECORD)) {
            criteria.addAll(parsePermission(permissions.getFilter(FormOperation.EDIT_RECORD)));
        }

        /*
         * Now map each of the criteria to a field, if possible
         */
        Multimap<ResourceId, FormulaNode> fieldCriteria = HashMultimap.create();
        for (FormulaNode criterium : criteria) {
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
            fieldFilters.put(fieldId, Formulas.allTrue(fieldCriteria.get(fieldId)));
        }
    }

    private List<FormulaNode> parsePermission(String filter) {
        FormulaNode formulaNode = FormulaParser.parse(filter);
        return Formulas.findBinaryTree(formulaNode, AndFunction.INSTANCE);
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
    public Optional<FormulaNode> getReferenceBaseFilter(ResourceId fieldId) {
        FormulaNode filter = fieldFilters.get(fieldId);
        if(filter == null) {
            return Optional.absent();
        }

        SymbolNode fieldExpr = new SymbolNode(fieldId);

        return Optional.of(filter.transform(x -> {
            if(x.equals(fieldExpr)) {
                return new SymbolNode(ColumnModel.ID_SYMBOL);
            } else {
                return x;
            }
        }));
    }
}
