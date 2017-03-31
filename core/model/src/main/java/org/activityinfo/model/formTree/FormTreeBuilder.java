package org.activityinfo.model.formTree;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordFieldType;

import java.util.List;
import java.util.logging.Logger;

/**
 * Constructs a tree of related Forms from a given root FormClass.
 */
public class FormTreeBuilder {

    private static final Logger LOGGER = Logger.getLogger(FormTreeBuilder.class.getName());

    private final FormClassProvider store;

    public FormTreeBuilder(FormClassProvider store) {
        this.store = store;
    }

    public FormTree queryTree(ResourceId rootFormClassId) {

        FormTree tree = new FormTree();
        FormClass rootClass = store.getFormClass(rootFormClassId);

        List<ResourceId> stack = Lists.newArrayList(rootFormClassId);

        Optional<FormField> parentField = rootClass.getParentField();
        if(parentField.isPresent()) {
            if(!stack.contains(parentField.get().getId())) {
                FormTree.Node node = tree.addRootField(rootClass, parentField.get());
                fetchChildren(stack, node, rootClass.getParentFormId().asSet());
            }
        }
        
        // Add fields defined by this FormClass
        for(FormField field : rootClass.getFields()) {
            FormTree.Node node = tree.addRootField(rootClass, field);
            if(node.isReference()) {
                fetchChildren(stack, node, node.getRange());
            } else if(field.getType() instanceof RecordFieldType) {
                addChildren(stack, node, ((RecordFieldType) field.getType()).getFormClass());
            }
        }
        return tree;
    }

    /**
     * Now that we have the actual FormClass model that corresponds to this node's
     * formClassId, add it's children.
     *
     */
    private void fetchChildren(List<ResourceId> stack, FormTree.Node parent, Iterable<ResourceId> formClassIds)  {
        for(ResourceId childClassId : formClassIds) {
            if(!stack.contains(childClassId)) {
                FormClass childClass = store.getFormClass(childClassId);
                assert childClass != null;
                addChildren(stack, parent, childClass);
            }
        }
    }

    private void addChildren(List<ResourceId> stack, FormTree.Node parent, FormClass childClass) {
        stack.add(parent.getDefiningFormClass().getId());
        for(FormField field : childClass.getFields()) {
            FormTree.Node childNode = parent.addChild(childClass, field);
            if(childNode.isReference()) {
               fetchChildren(stack, childNode, childNode.getRange());
            } else if(childNode.getType() instanceof RecordFieldType) {
                addChildren(stack, childNode, ((RecordFieldType) childNode.getType()).getFormClass());
            }
        }
        stack.remove(stack.size() - 1);
    }

}
