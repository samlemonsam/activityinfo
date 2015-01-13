package org.activityinfo.model.expr.eval;


import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.expr.diagnostic.AmbiguousSymbolException;
import org.activityinfo.model.expr.diagnostic.SymbolNotFoundException;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;

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

    public SymbolBinding resolveSymbol(SymbolExpr symbol) {
        return match(symbol.getName(), tree.getRootFields());
    }

    public SymbolBinding resolveCompoundExpr(List<FormTree.Node> fields, CompoundExpr expr) {
        if(expr.getValue() instanceof SymbolExpr) {
            SymbolBinding parentField = match(((SymbolExpr) expr.getValue()).getName(), fields);
            Iterable children = Iterables.filter(fields, parentField.getChildPredicate());

            return match(expr.getField().getName(), children);

        } else if(expr.getValue() instanceof CompoundExpr) {
            return resolveCompoundExpr(fields, (CompoundExpr) expr.getValue());

        } else {
            throw new UnsupportedOperationException("Unexpected value of compound expr: " + expr.getValue());
        }
    }


    /**
     * Matches a symbol against the fields that are present.
     *
     * @param name the symbol name to resolve
     * @param fields the fields that are present at this level in the tree
     */
    private SymbolBinding match(String name, Iterable<FormTree.Node> fields) {

        // first try to resolve by id.
        for(FormTree.Node rootField : fields) {
            if(rootField.getFieldId().asString().equals(name)) {
                return new SymbolBinding(rootField);
            }
        }

        // then try to resolve the field by the code or label
        List<SymbolBinding> matching = Lists.newArrayList();
        collectMatching(name, fields, matching);

        if(matching.size() == 1) {
            return matching.get(0);

        } else if(matching.isEmpty()) {
            throw new SymbolNotFoundException(name);

        } else {
            throw new AmbiguousSymbolException(name, "Could refer to : " +
                    Joiner.on(", ").join(matching));
        }
    }

    /**
     * Collect all the possible matches against this symbol within the tree
     */
    private void collectMatching(String symbolName, Iterable<FormTree.Node> fields, List<SymbolBinding> matching) {
        boolean matched = false;
        for(FormTree.Node fieldNode : fields) {
            SymbolBinding match = matches(symbolName, fieldNode);
            if(match != null) {
                matching.add(match);
                matched = true;
            }
        }
        // if we do not have a direct match, consider descendants
        if(!matched) {
            for(FormTree.Node field : fields) {
                collectMatching(symbolName, field.getChildren(), matching);
            }
        }
    }

    private SymbolBinding matches(String symbolName, FormTree.Node fieldNode) {

        // Match against label and code case insensitively
        if(symbolName.equalsIgnoreCase(fieldNode.getField().getCode()) ||
                symbolName.equalsIgnoreCase(fieldNode.getField().getLabel())) {
            return new SymbolBinding(fieldNode);
        }
        // Require exact match with the field id
        if(symbolName.equals(fieldNode.getFieldId().asString())) {
            return new SymbolBinding(fieldNode);
        }

        // Check for super properties defined on the FormClass
        for(ResourceId superProperty : fieldNode.getField().getSuperProperties()) {
            if(symbolName.equals(superProperty.asString())) {
                return new SymbolBinding(fieldNode);
            }
        }

        if(fieldNode.getType() instanceof ReferenceType) {
            ReferenceType fieldType = (ReferenceType) fieldNode.getType();
            for(ResourceId formClassId : fieldType.getRange()) {
                if(formClassId.asString().equals(symbolName)) {
                    return new SymbolBinding(fieldNode, formClassId);
                }
            }
        }
        return null;
    }


}
