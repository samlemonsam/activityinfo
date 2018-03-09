/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
