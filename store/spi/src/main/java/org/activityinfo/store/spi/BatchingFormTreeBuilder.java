package org.activityinfo.store.spi;

import com.google.common.collect.Sets;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.subform.SubFormReferenceType;

import java.util.*;
import java.util.logging.Logger;

public class BatchingFormTreeBuilder {


    private static final Logger LOGGER = Logger.getLogger(BatchingFormTreeBuilder.class.getName());

    private final FormCatalog catalog;

    private final Map<ResourceId, FormClass> formCache = new HashMap<>();

    public BatchingFormTreeBuilder(FormCatalog catalog) {
        this.catalog = catalog;
    }

    public Map<ResourceId, FormTree> queryTrees(Collection<ResourceId> rootFormIds) {

        // Fetch Required FormClasses in batches

        Set<ResourceId> toFetch = Sets.newHashSet();
        toFetch.addAll(rootFormIds);

        while(!toFetch.isEmpty()) {

            // First round: fetch root form classes
            List<FormClass> fetched = fetchFormClasses(toFetch);
            toFetch.clear();

            // Find newly referenced forms
            for (FormClass formClass : fetched) {
                if(formClass.isSubForm()) {
                    if(!formCache.containsKey(formClass.getParentFormId().get())) {
                        toFetch.add(formClass.getParentFormId().get());
                    }
                }
                for (FormField formField : formClass.getFields()) {
                    if(formField.getType() instanceof ReferenceType) {
                        ReferenceType refType = (ReferenceType) formField.getType();
                        for (ResourceId refFormId : refType.getRange()) {
                            if(!formCache.containsKey(refFormId)) {
                                toFetch.add(refFormId);
                            }
                        }
                    } else if(formField.getType() instanceof SubFormReferenceType) {
                        SubFormReferenceType subFormType = (SubFormReferenceType) formField.getType();
                        if(!formCache.containsKey(subFormType.getClassId())) {
                            toFetch.add(subFormType.getClassId());
                        }
                    }
                }
            }
        }

        // Now assemble trees
        Map<ResourceId, FormTree> treeMap = new HashMap<>();
        FormTreeBuilder builder = new FormTreeBuilder(new FormClassProvider() {
            @Override
            public FormClass getFormClass(ResourceId formId) {
                return formCache.get(formId);
            }
        });

        for (ResourceId rootFormId : rootFormIds) {
            treeMap.put(rootFormId, builder.queryTree(rootFormId));
        }

        return treeMap;
    }


    private List<FormClass> fetchFormClasses(Iterable<ResourceId> formIds) {

        List<FormClass> fetched = new ArrayList<>();

        // Identify the forms that we don't already have in the cache
        Set<ResourceId> toFetch = new HashSet<>();
        for (ResourceId formClassId : formIds) {
            if(!formCache.containsKey(formClassId)) {
                toFetch.add(formClassId);
            }
        }
        // Fetch from the store
        Map<ResourceId, FormClass> formClasses = catalog.getFormClasses(toFetch);

        // Store back to the cache
        for (FormClass formClass : formClasses.values()) {
            formCache.put(formClass.getId(), formClass);
            fetched.add(formClass);
        }

        return fetched;
    }
}
