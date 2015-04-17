package org.activityinfo.geoadmin.merge;

import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.geo.GeoArea;
import org.activityinfo.model.type.primitive.TextType;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a form that is the target of updates in a merge operation.
 */
public class TargetForm {
    
    private final FormTree tree;
    private final List<FormTree.Node> textFields = new ArrayList<>();

    public TargetForm(FormTree tree) {
        this.tree = tree;
        visitFields(tree.getRootFields());
    }

    private void visitFields(List<FormTree.Node> nodes) {
        for (FormTree.Node node : nodes) {
            if(node.getType() instanceof TextType) {
                textFields.add(node);
            } else if(node.getType() instanceof GeoArea) {
                areaFields.add(node);
            } else if(node.isReference()) {
                visitFields(node.getChildren());
            }
        }
    }
    
    
}
