package org.activityinfo.test.api;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;

/**
 * An ActivityInfo database created on the server for testing
 */
public class TestDatabase {

    private int id;
    private String name;
    private ResourceId firstPartnerId;

    public TestDatabase(ResourceId resourceId, String name, ResourceId firstPartnerId) {
        this.name = name;
        this.id = CuidAdapter.getLegacyIdFromCuid(resourceId);

        this.firstPartnerId = firstPartnerId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ResourceId getPartnerFormId() {
        return CuidAdapter.partnerFormId(id);
    }

    public ReferenceValue getDefaultPartner() {
        return new ReferenceValue(new RecordRef(getPartnerFormId(), firstPartnerId));
    }
}
