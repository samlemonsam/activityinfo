package org.activityinfo.model.expr.eval;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.expr.diagnostic.AmbiguousSymbolException;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Resolves symbols in queries to the fields on the base FormClass
 * or on related tables.
 *
 * TODO: Harmonize with FormSymbolTable. Can we use only FormTreeSymbolTable?
 */
public class FormTreeSymbolTable {

    private final FormTree tree;

    public FormTreeSymbolTable(FormTree formTree) {
        this.tree = formTree;
    }

    public Collection<FormTree.Node> resolveSymbol(SymbolExpr symbol) {

        LinkedList<String> queryPath = new LinkedList<>();
        queryPath.push(symbol.getName());

        return matchNodes(queryPath, tree.getRootFields());
    }

    /**
     * Resolves a compound expression like "province.name" to one or more {@code FormTree.Nodes}
     *
     * @return a binding to the corresponding {@code FormTree.Node}
     *
     * @throws AmbiguousSymbolException if the expression could match multiple nodes in the tree
     */
    public Collection<FormTree.Node> resolveCompoundExpr(CompoundExpr expr) {

        LinkedList<String> queryPath = toQueryPath(expr);

        Collection<FormTree.Node> results = matchNodes(queryPath, tree.getRootFields());

        return results;
    }


    /**
     * Converts a tree of {@code CompoundExpr}s and {@code SymbolExpr}s to a {@code LinkedList} of 
     * names to follow.
     */
    @VisibleForTesting
    static LinkedList<String> toQueryPath(CompoundExpr expr) {

        // Recursively convert the compound expr to a linked list of symbols

        LinkedList<String> queryPath = new LinkedList<>();
        while(true) {
            queryPath.push(expr.getField().getName());
            if(expr.getValue() instanceof CompoundExpr) {
                expr = (CompoundExpr) expr.getValue();
            } else if(expr.getValue() instanceof SymbolExpr) {
                queryPath.push(((SymbolExpr) expr.getValue()).getName());
                break;
            }
        }
        return queryPath;
    }


    private Collection<FormTree.Node> matchNodes(List<String> queryPath, Iterable<FormTree.Node> fields) {
        if(queryPath.size() == 1) {
            return matchTerminal(head(queryPath), fields);
        } else {
            return matchReferenceField(queryPath, fields);
        }
    }

    private List<String> next(List<String> queryPath) {
        return queryPath.subList(1, queryPath.size());
    }

    private String head(List<String> queryPath) {
        return queryPath.get(0);
    }

    private Collection<FormTree.Node> matchReferenceField(List<String> queryPath, Iterable<FormTree.Node> fields) {

        List<Collection<FormTree.Node>> results = Lists.newArrayList();

        for (FormTree.Node field : fields) {
            Collection<FormTree.Node> result = unionMatches(queryPath, field);
            if (!result.isEmpty()) {
                results.add(result);
            }
        }
        if(results.size() > 1) {
            throw new AmbiguousSymbolException(Joiner.on('.').join(queryPath));
        } else if(results.size() == 1) {
            return results.get(0);
        }

        // If no results, check search at the next level
        List<FormTree.Node> children = childrenOf(fields);
        if(children.isEmpty()) {
            return Collections.emptyList();
        } else {
            return matchReferenceField(queryPath, children);
        }
    }

    private Collection<FormTree.Node> unionMatches(List<String> queryPath, FormTree.Node referenceField) {
        List<FormTree.Node> results = Lists.newArrayList();
        for (ResourceId formClassId : referenceField.getRange()) {
            FormClass childForm = tree.getFormClass(formClassId);
            Iterable<FormTree.Node> childFields = referenceField.getChildren(formClassId);

            if(matches(head(queryPath), referenceField) || matches(head(queryPath), childForm)) {
                results.addAll(matchNodes(next(queryPath), childFields));
            } else {
                results.addAll(matchNodes(queryPath, childFields));
            }
        }
        return results;
    }


    /**
     * Matches a terminal symbol in a query path.
     * @param symbolName the symbol name
     * @param fields the fields against which to match
     */
    private Collection<FormTree.Node> matchTerminal(String symbolName, Iterable<FormTree.Node> fields) {

        List<FormTree.Node> matches = Lists.newLinkedList();

        // Check for a match of the query Path head to the set of fields
        for (FormTree.Node field : fields) {
            if(matches(symbolName, field)) {
                matches.add(field);
            }
        }

        // If there is exactly one matching field, then we consider it a good match
        // and we return 
        if(matches.size() == 1) {
            return matches;
        }

        // If there is MORE than one match, we consider the expression to be ambiguous
        if(matches.size() > 1) {
            throw new AmbiguousSymbolException(symbolName, "Could refer to " + Joiner.on(", ").join(matches));
        }

        // If we found absolutely nothing, then continue to the next level
        List<FormTree.Node> children = childrenOf(fields);
        if(children.isEmpty()) {
            return Collections.emptyList();
        } else {
            return matchTerminal(symbolName, children);
        }
    }


    private List<FormTree.Node> childrenOf(Iterable<FormTree.Node> fields) {
        List<FormTree.Node> children = Lists.newArrayList();
        for (FormTree.Node field : fields) {
            children.addAll(field.getChildren());
        }
        return children;
    }

    private boolean matches(String symbolName, FormTree.Node fieldNode) {

        // Match against label and code case insensitively
        if (symbolName.equalsIgnoreCase(fieldNode.getField().getCode()) ||
                symbolName.equalsIgnoreCase(fieldNode.getField().getLabel())) {
            return true;
        }
        // Require exact match with the field id
        if (symbolName.equals(fieldNode.getFieldId().asString())) {
            return true;
        }

        // Check for super properties defined on the FormClass
        for (ResourceId superProperty : fieldNode.getField().getSuperProperties()) {
            if (symbolName.equals(superProperty.asString())) {
                return true;
            }
        }

        return false;
    }

    private boolean matches(String symbolName, FormClass formClass) {

        // The field can also be matched against the _range_ of a field: for example,
        // we might be interested in "Province.Name", where "Province" is a form class.
        // In this event, match any reference field which includes in its range a form class with
        // the id or label of "Province"
        return formClass.getLabel().equalsIgnoreCase(symbolName) ||
                formClass.getId().equals(symbolName);
    }

}
