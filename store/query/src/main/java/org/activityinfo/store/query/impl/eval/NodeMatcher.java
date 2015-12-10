package org.activityinfo.store.query.impl.eval;


import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.expr.diagnostic.AmbiguousSymbolException;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Resolves symbols in queries to the fields on the base FormClass
 * or on related tables.
 *
 * TODO: Harmonize with FormSymbolTable. Do we need both?
 */
public class NodeMatcher {

    private final FormTree tree;

    public NodeMatcher(FormTree formTree) {
        this.tree = formTree;
    }

    public Collection<NodeMatch> resolveSymbol(SymbolExpr symbol) {
        return matchNodes(new QueryPath(symbol), tree.getRootFields());
    }

    /**
     * Resolves a compound expression like "province.name" to one or more {@code FormTree.Nodes}
     *
     * @return a binding to the corresponding {@code FormTree.Node}
     *
     * @throws AmbiguousSymbolException if the expression could match multiple nodes in the tree
     */
    public Collection<NodeMatch> resolveCompoundExpr(CompoundExpr expr) {
        return matchNodes(new QueryPath(expr), tree.getRootFields());
    }

    private Collection<NodeMatch> matchNodes(QueryPath queryPath, Iterable<FormTree.Node> fields) {
        if(queryPath.isLeaf()) {
            return matchTerminal(queryPath, fields);
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

    private Collection<NodeMatch> matchReferenceField(QueryPath queryPath, Iterable<FormTree.Node> fields) {

        List<Collection<NodeMatch>> matches = Lists.newArrayList();

        for (FormTree.Node field : fields) {
            Collection<NodeMatch> result = unionMatches(queryPath, field);
            if (!result.isEmpty()) {
                matches.add(result);
            }
        }
        if(matches.size() > 1) {
            throw new AmbiguousSymbolException(queryPath.toString());
        } else if(matches.size() == 1) {
            return matches.get(0);
        }

        // If no results, check search at the next level
        List<FormTree.Node> children = childrenOf(fields);
        if(children.isEmpty()) {
            return Collections.emptyList();
        } else {
            return matchReferenceField(queryPath, children);
        }
    }


    /**
     * Matches a terminal symbol in a query path.
     * @param symbolName the symbol name
     * @param fields the fields against which to match
     */
    private Collection<NodeMatch> matchTerminal(QueryPath path, Iterable<FormTree.Node> fields) {

        List<NodeMatch> matches = Lists.newLinkedList();

        // Check for a match of the query Path head to the set of fields
        for (FormTree.Node field : fields) {
            if(path.matches(field)) {
                matches.add(new NodeMatch(field));
            }
        }

        // If there is exactly one matching field, then we consider it a good match
        // and we return 
        if(matches.size() == 1) {
            return matches;
        }

        // If there is MORE than one match, we consider the expression to be ambiguous
        if(matches.size() > 1) {
            throw new AmbiguousSymbolException(path.head(), "Could refer to " + Joiner.on(", ").join(matches));
        }

        // If we found absolutely nothing, then continue to the next level
        List<FormTree.Node> children = childrenOf(fields);
        if(children.isEmpty()) {
            return Collections.emptyList();
        } else {
            return matchTerminal(path, children);
        }
    }

    private Collection<NodeMatch> unionMatches(QueryPath path, FormTree.Node referenceField) {
        List<NodeMatch> results = Lists.newArrayList();
        for (ResourceId formClassId : referenceField.getRange()) {
            FormClass childForm = tree.getFormClass(formClassId);
            Iterable<FormTree.Node> childFields = referenceField.getChildren(formClassId);

            if(path.matches(childForm) && path.peek().equals(ColumnModel.ID_SYMBOL)) {
                results.add(NodeMatch.id(referenceField, childForm));

            } else if(path.matches(childForm) || path.matches(referenceField)) {
                results.addAll(matchNodes(path.next(), childFields));
                
            } else {
                // Descend the next level
                results.addAll(matchNodes(path, childFields));
            }
        }
        return results;
    }


    private List<FormTree.Node> childrenOf(Iterable<FormTree.Node> fields) {
        List<FormTree.Node> children = Lists.newArrayList();
        for (FormTree.Node field : fields) {
            children.addAll(field.getChildren());
        }
        return children;
    }

    private boolean matches(FormTree.Node fieldNode, String symbolName) {

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

    private boolean matches(FormClass formClass, String symbolName) {

        // The field can also be matched against the _range_ of a field: for example,
        // we might be interested in "Province.Name", where "Province" is a form class.
        // In this event, match any reference field which includes in its range a form class with
        // the id or label of "Province"
        return formClass.getLabel().equalsIgnoreCase(symbolName) ||
                formClass.getId().asString().equals(symbolName);
    }

}
