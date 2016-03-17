package org.activityinfo.service.store;

import com.google.common.collect.Sets;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;

import java.util.*;
import java.util.logging.Logger;

public class BatchingFormTreeBuilder {


    private static final Logger LOGGER = Logger.getLogger(BatchingFormTreeBuilder.class.getName());

    private final CollectionCatalog catalog;

    private final Map<ResourceId, FormClass> formCache = new HashMap<>();
    
    public BatchingFormTreeBuilder(CollectionCatalog catalog) {
        this.catalog = catalog;
    }

    public Map<ResourceId, FormTree> queryTrees(Collection<ResourceId> rootFormClassIds) {

        Map<ResourceId, FormTree> treeMap = new HashMap<>();

        Set<FormTree.Node> parentNodes = new HashSet<>();

        // First round: fetch root form classes
        fetchFormClasses(rootFormClassIds);
        
        for (ResourceId rootClassId : rootFormClassIds) {
            FormTree tree = new FormTree();
            FormClass rootForm = formCache.get(rootClassId);
            for (FormField field : rootForm.getFields()) {
                FormTree.Node node = tree.addRootField(rootForm, field);
                if (field.getType() instanceof ReferenceType) {
                    parentNodes.add(node);
                }
            }
            treeMap.put(rootClassId, tree);
        }

        // Round 2+ : Fetch form classes of children
        while(!parentNodes.isEmpty()) {
            
            Set<ResourceId> toFetch = Sets.newHashSet();

            for (FormTree.Node parentNode : parentNodes) {
                
                // For each of the parentNodes and their potential range classes, fetch those
                // that are not yet in our cache
                for (ResourceId rangeFormClassId : parentNode.getRange()) {
                    if(!formCache.containsKey(rangeFormClassId)) {
                        toFetch.add(rangeFormClassId);
                    }
                }
            }

            // Fetch this next level of form Classes
            fetchFormClasses(toFetch);

            // Add child fields
            Set<FormTree.Node> nextParents = new HashSet<>();
            for (FormTree.Node parentNode : parentNodes) {
                for (ResourceId rangeFormClassId : parentNode.getRange()) {
                    FormClass childClass = formCache.get(rangeFormClassId);
                    for (FormField childField : childClass.getFields()) {
                        FormTree.Node childNode = parentNode.addChild(childClass, childField);
                        if(childNode.isReference()) {
                            nextParents.add(childNode);
                        }
                    }
                }
            }
            parentNodes = nextParents;
        }
        return treeMap;
    }


    private void fetchFormClasses(Iterable<ResourceId> formClassIds) {
        // Identify the forms that we don't already have in the cache
        Set<ResourceId> toFetch = new HashSet<>();
        for (ResourceId formClassId : formClassIds) {
            if(!formCache.containsKey(formClassId)) {
                toFetch.add(formClassId);
            }
        }
        // Fetch from the store
        Map<ResourceId, FormClass> formClasses = catalog.getFormClasses(toFetch);
        
        // Store back to the cache
        for (FormClass formClass : formClasses.values()) {
            formCache.put(formClass.getId(), formClass);
        }
    }
}
