package org.activityinfo.ui.client.store;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.formTree.FormMetadataProvider;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableTree;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class FormTreeLoader implements ObservableTree.TreeLoader<ResourceId, FormMetadata, FormTree> {

    private final ResourceId rootFormId;
    private final Function<ResourceId, Observable<FormMetadata>> provider;

    public FormTreeLoader(ResourceId rootFormId, Function<ResourceId, Observable<FormMetadata>> provider) {
        this.rootFormId = rootFormId;
        this.provider = provider;
    }

    @Override
    public ResourceId getRootKey() {
        return rootFormId;
    }

    @Override
    public Observable<FormMetadata> get(ResourceId formId) {
        return provider.apply(formId);
    }

    @Override
    public Iterable<ResourceId> getChildren(FormMetadata node) {

        Set<ResourceId> children = new HashSet<>();

        if(node.isAccessible()) {

            assert node.getSchema() != null : "Missing schema " + node.getId();

            if(node.getSchema().isSubForm()) {
                children.add(node.getSchema().getParentFormId().get());
            }

            for (FormField field : node.getFields()) {
                if (field.getType() instanceof ReferenceType) {
                    ReferenceType type = (ReferenceType) field.getType();
                    for (ResourceId childId : type.getRange()) {
                        children.add(childId);
                    }
                } else if (field.getType() instanceof SubFormReferenceType) {
                    SubFormReferenceType type = (SubFormReferenceType) field.getType();
                    children.add(type.getClassId());
                }
            }
        }
        return children;
    }

    @Override
    public FormTree build(Map<ResourceId, Observable<FormMetadata>> nodes) {
        FormTreeBuilder builder = new FormTreeBuilder(new FormMetadataProvider() {
            @Override
            public FormMetadata getFormMetadata(ResourceId formId) {
                Observable<? extends FormMetadata> metadata = nodes.get(formId);
                assert metadata != null : "Form " + formId + " is missing!";
                assert !metadata.isLoading() : "Form " + formId + " is still loading!";

                return metadata.get();
            }
        });

        return builder.queryTree(rootFormId);
    }
}
