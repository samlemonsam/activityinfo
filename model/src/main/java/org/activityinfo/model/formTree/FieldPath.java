package org.activityinfo.model.formTree;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.resource.ResourceId;

import java.util.Iterator;
import java.util.List;

/**
 * Describes a path of nested fields
 */
public class FieldPath  {

    private final List<ResourceId> path;

    public FieldPath(ResourceId rootFieldId, FieldPath relativePath) {
        path = Lists.newArrayList(rootFieldId);
        path.addAll(relativePath.path);
    }

    public FieldPath(FieldPath prefix, ResourceId key) {
        path = Lists.newArrayList();
        if(prefix != null) {
            path.addAll(prefix.path);
        }
        path.add(key);
    }

    public FieldPath(FieldPath parent, FieldPath relativePath) {
        this.path = Lists.newArrayList();
        this.path.addAll(parent.path);
        this.path.addAll(relativePath.path);
    }

    public FieldPath(List<ResourceId> fieldIds) {
        path = Lists.newArrayList(fieldIds);
    }

    public FieldPath(ResourceId... fieldIds) {
        path = Lists.newArrayList(fieldIds);
    }


    public boolean isNested() {
        return path.size() > 1;
    }


    public int getDepth() {
        return path.size();
    }

    public FieldPath relativeTo(ResourceId rootFieldId) {
        Preconditions.checkArgument(path.get(0).equals(rootFieldId));
        return new FieldPath(path.subList(1, path.size()));
    }

    public ResourceId getLeafId() {
        return path.get(path.size()-1);
    }

    /**
     * Creates a new FieldPath that describes the nth ancestor of this
     * path. n=1 is the direct parent, n=2, grand parent, etc.
     */
    public FieldPath ancestor(int n) {
        return new FieldPath(path.subList(0, path.size()-n));
    }

    public ResourceId getRoot() {
        return path.get(0);
    }

    public boolean isDescendantOf(ResourceId fieldId) {
        return path.get(0).equals(fieldId);
    }

    public List<ResourceId> getPath() {
        return path;
    }

    public FormulaNode toExpr() {
        Iterator<ResourceId> it = path.iterator();
        FormulaNode expr = new SymbolNode(it.next());
        while(it.hasNext()) {
            expr = new CompoundExpr(expr, new SymbolNode(it.next()));
        }
        return expr;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for(ResourceId fieldId : path) {
            if(s.length() > 0) {
                s.append(".");
            }
            s.append(fieldId.asString());
        }
        return s.toString();
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FieldPath)) {
            return false;
        }
        FieldPath otherPath = (FieldPath) obj;

        return path.equals(otherPath.path);
    }

    public Iterator<ResourceId> iterator() {
        return path.iterator();
    }

    public boolean isRoot() {
        return !isNested();
    }
}
