package org.activityinfo.ui.client.store;


import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.TransactionBuilder;
import org.activityinfo.model.resource.UpdateBuilder;
import org.activityinfo.model.type.RecordRef;

import java.util.HashSet;
import java.util.Set;

public interface FormChange {

    boolean isFormChanged(ResourceId formId);

    boolean isSchemaChanged(ResourceId formId);

    boolean isRecordChanged(RecordRef recordRef);

    static FormChange from(TransactionBuilder transactionBuilder) {

        Set<ResourceId> updatedForms = new HashSet<>();

        for (UpdateBuilder updateBuilder : transactionBuilder.getUpdates()) {
            updatedForms.add(updateBuilder.getFormId());
        }

        return new FormChange() {
            @Override
            public boolean isFormChanged(ResourceId formId) {
                return updatedForms.contains(formId);
            }

            @Override
            public boolean isSchemaChanged(ResourceId formId) {
                return false;
            }

            @Override
            public boolean isRecordChanged(RecordRef recordRef) {
                return updatedForms.contains(recordRef.getFormId());
            }
        };
    }


}
