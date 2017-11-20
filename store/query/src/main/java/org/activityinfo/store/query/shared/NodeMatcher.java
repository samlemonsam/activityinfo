package org.activityinfo.store.query.shared;


import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.expr.diagnostic.AmbiguousSymbolException;
import org.activityinfo.model.expr.functions.ExprFunction;
import org.activityinfo.model.expr.functions.StatFunction;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordFieldType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;

import java.util.*;

/**
 * Resolves symbols in queries to the fields on the base FormClass
 * or on related tables.
 *
 * TODO: Harmonize with FormSymbolTable. Do we need both?
 */
public class NodeMatcher {

    private final FormTree tree;

    @SuppressWarnings("NonJREEmulationClassesInClientCode") /* Supported in GWT 2.8+ */
    private Deque<StatFunction> aggregationContextStack = new ArrayDeque<>();

    public NodeMatcher(FormTree formTree) {
        this.tree = formTree;
    }

    /**
     * Called directly before the QueryEvaluator evaluates a function. If the function is
     * a {@code StatFunction}, it will be added to the context.
     *
     * For example, if we encounter the expression MAX(A), and A is resolved to a symbol
     * in a subform, then we know that we have to apply the MAX() function for each row in the parent form
     * to all of the values in the subform for that record.
     *
     */
    public void enterFunction(ExprFunction function) {
        if(function instanceof StatFunction) {
            aggregationContextStack.push((StatFunction) function);
        }
    }

    public void exitFunction(ExprFunction function) {
        if(function instanceof StatFunction) {
            aggregationContextStack.pop();
        }
    }

    private Optional<StatFunction> currentAggregation() {
        return Optional.fromNullable(aggregationContextStack.peek());
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
        QueryPath queryPath = new QueryPath(expr);

        // Given an expression like Province.Name, see if we can match the
        // the first symbol against the root form's id or label
        if(queryPath.matches(tree.getRootFormClass())) {
            return matchNodes(queryPath.next(), tree.getRootFields());
        }

        return matchNodes(queryPath, tree.getRootFields());
    }

    private Collection<NodeMatch> matchNodes(QueryPath queryPath, Iterable<FormTree.Node> fields) {
        if(queryPath.isLeaf()) {
            return matchTerminal(queryPath, fields);
        } else {
            return matchReferenceField(queryPath, fields);
        }
    }

    private Collection<NodeMatch> matchReferenceField(QueryPath queryPath, Iterable<FormTree.Node> fields) {

        List<Collection<NodeMatch>> matches = Lists.newArrayList();

        for (FormTree.Node field : fields) {
            if(field.getType() instanceof ReferenceType || 
               field.getType() instanceof RecordFieldType) {
                Collection<NodeMatch> result = unionMatches(queryPath, field);
                if (!result.isEmpty()) {
                    matches.add(result);
                }
            } else if(field.getType() instanceof EnumType) {
                Optional<NodeMatch> result = matchEnum(queryPath, field);
                if(result.isPresent()) {
                    matches.add(Collections.singleton(result.get()));
                }
            }
        }
        if(matches.size() > 0) {
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

    private Optional<NodeMatch> matchEnum(QueryPath queryPath, FormTree.Node field) {
        if(queryPath.matches(field)) {
            QueryPath next = queryPath.next();
            if(next.isLeaf()) {
                EnumType type = (EnumType) field.getType();
                List<EnumItem> matchingItems = Lists.newArrayList();
                for (EnumItem enumItem : type.getValues()) {
                    if(next.head().equals(enumItem.getId().asString()) ||
                       next.head().equalsIgnoreCase(enumItem.getLabel()) ||
                       next.head().equalsIgnoreCase(enumItem.getCode())) {
                        
                        matchingItems.add(enumItem);
                    }
                }
                if(matchingItems.size() == 1) {
                    return Optional.of(NodeMatch.forEnumItem(field, matchingItems.get(0)));
                } 
            }
        }
        return Optional.absent();
    }


    /**
     * Matches a terminal symbol in a query path.
     */
    private Collection<NodeMatch> matchTerminal(QueryPath path, Iterable<FormTree.Node> fields) {

        List<NodeMatch> matches = Lists.newLinkedList();

        // Check for a match of the query Path head to the set of fields
        for (FormTree.Node field : fields) {
            if(path.matches(field)) {
                matches.add(NodeMatch.forField(field, currentAggregation()));
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

    private Collection<NodeMatch> unionMatches(QueryPath path, FormTree.Node parentField) {
        List<NodeMatch> results = Lists.newArrayList();
        for (ResourceId formClassId : parentField.getRange()) {
            FormClass childForm = tree.getFormClass(formClassId);
            Iterable<FormTree.Node> childFields = parentField.getChildren(formClassId);

            if(path.matches(childForm) && path.peek().equals(ColumnModel.ID_SYMBOL)) {
                results.add(NodeMatch.forId(parentField, childForm));

            } else if(path.matches(childForm) || path.matches(parentField)) {
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


}
