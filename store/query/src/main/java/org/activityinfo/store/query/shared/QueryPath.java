package org.activityinfo.store.query.shared;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;

import java.util.LinkedList;
import java.util.List;

/**
 * Dotted query path, for example "Province.Name.@id"
 */
public class QueryPath {
    
    @VisibleForTesting
    final List<String> path;
    
    public QueryPath(SymbolExpr expr) {
        path = Lists.newArrayList(expr.getName());
    }
    
    public QueryPath(CompoundExpr expr) {
        path = new LinkedList<>();

        // Recursively convert the compound expr to a linked list of symbols
        while(true) {
            path.add(0, expr.getField().getName());
            if(expr.getValue() instanceof CompoundExpr) {
                expr = (CompoundExpr) expr.getValue();
        
            } else if(expr.getValue() instanceof SymbolExpr) {
                path.add(0, ((SymbolExpr) expr.getValue()).getName());
                break;
            }
        }
    }

    private QueryPath(List<String> path) {
        this.path = path;
    }


    /**
     * 
     * @return the head, or the first element, in this query path
     */
    public String head() {
        return path.get(0);
    }

    /**
     * 
     * @return the next element in this query path, or an empty string if there is no next element
     */
    public String peek() {
        if(path.size() > 1) {
            return path.get(1);
        } else {
            return "";
        }
    }

    /**
     * 
     * @return a new {@code QueryPath} with the next element as the new head.
     */
    public QueryPath next() {
        return new QueryPath(path.subList(1, path.size()));
    }


    /**
     * @return true if the current head of this query path matches the given {@code fieldNode}
     */
    public boolean matches(FormTree.Node fieldNode) {
        
        String symbolName = path.get(0);
        

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


    /**
     * @return true if the current head of this query path matches the given {@code formClass}
     */
    public boolean matches(FormClass formClass) {

        String symbolName = path.get(0);

        // The field can also be matched against the _range_ of a field: for example,
        // we might be interested in "Province.Name", where "Province" is a form class.
        // In this event, match any reference field which includes in its range a form class with
        // the id or label of "Province"
        return formClass.getLabel().equalsIgnoreCase(symbolName) ||
                formClass.getId().asString().equals(symbolName);
    }

    /**
     * 
     * @return true if the head of this query path is the last element in the path.
     */
    public boolean isLeaf() {
        return path.size() == 1;
    }

    @Override
    public String toString() {
        return Joiner.on(".").join(path);
    }
}
