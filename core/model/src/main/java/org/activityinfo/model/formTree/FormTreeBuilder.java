package org.activityinfo.model.formTree;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;

import java.util.Set;
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

        // Add fields defined by this FormClass
        for(FormField field : rootClass.getFields()) {
            FormTree.Node node = tree.addRootField(rootClass, field);
            if(field.getType() instanceof ReferenceType) {
                ReferenceType referenceType = (ReferenceType) field.getType();
                fetchChildren(node, referenceType.getRange());
            }
//            } else if(field.getType() instanceof RecordFieldType) {
//                RecordFieldType recordFieldType = (RecordFieldType) field.getType();
//                fetchChildren(node, Collections.singleton(recordFieldType.getClassId()));
//            }
        }
        return tree;
    }

    /**
     * Now that we have the actual FormClass model that corresponds to this node's
     * formClassId, add it's children.
     *
     */
    private void fetchChildren(FormTree.Node parent, Set<ResourceId> formClassIds)  {
        for(ResourceId childClassId : formClassIds) {
            FormClass childClass = store.getFormClass(childClassId);
            assert childClass != null;
            for(FormField field : childClass.getFields()) {
                FormTree.Node childNode = parent.addChild(childClass, field);
                if(childNode.isReference()) {
                   fetchChildren(childNode, childNode.getRange());
                }
            }
        }
    }

}
