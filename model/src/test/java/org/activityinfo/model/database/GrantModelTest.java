package org.activityinfo.model.database;

import com.google.common.base.Optional;
import org.activityinfo.json.JsonParser;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.permission.Operation;
import org.activityinfo.model.resource.ResourceId;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GrantModelTest {

    private static final JsonParser PARSER = new JsonParser();

    private static final ResourceId RESOURCE = CuidAdapter.databaseId(1);
    private static final ResourceId PARTNER_FORM = CuidAdapter.partnerFormId(1111);
    private static final ResourceId PARTNER_RECORD = CuidAdapter.partnerRecordId(1234);

    @Test
    public void construction() {
        GrantModel model = buildModel();

        // Check correct construction
        assertThat(model.getResourceId(), equalTo(RESOURCE));
        assertThat(model.getOperations().size(), equalTo(3));
        assertThat(model.getOperations(), containsInAnyOrder(Operation.VIEW, Operation.EDIT_RECORD, Operation.MANAGE_USERS));
        assertThat(model.getFilter(Operation.VIEW), equalTo(Optional.of(partnerFilter())));
        assertThat(model.getFilter(Operation.EDIT_RECORD), equalTo(Optional.of(partnerFilter())));
        assertThat(model.getFilter(Operation.MANAGE_USERS), equalTo(Optional.absent()));
    }

    @Test
    public void serialization() {
        GrantModel model = buildModel();
        String serializedModel = model.toJson().toJson();
        JsonValue json = PARSER.parse(serializedModel);
        GrantModel deserializedModel = GrantModel.fromJson(json);

        // Check deserialized model is correct
        assertEquals(model.getResourceId(), deserializedModel.getResourceId());
        assertEquals(model.getOperations(), deserializedModel.getOperations());
        assertEquals(model.getFilter(Operation.VIEW), deserializedModel.getFilter(Operation.VIEW));
        assertEquals(model.getFilter(Operation.MANAGE_USERS), deserializedModel.getFilter(Operation.MANAGE_USERS));
    }

    private GrantModel buildModel() {
        // Create Grant Model
        GrantModel.Builder builder = new GrantModel.Builder();

        builder.setResourceId(RESOURCE);
        // Initially grant View All permissions, and then add a filter for Partner 1234
        builder.addOperation(Operation.VIEW);
        builder.addFilter(Operation.VIEW, partnerFilter());
        // Grant Edit on Partner 1234 permissions
        builder.addOperation(Operation.EDIT_RECORD, partnerFilter());
        // Grant Permission to Manage All Users
        builder.addOperation(Operation.MANAGE_USERS);

        return builder.build();
    }

    private String partnerFilter() {
        return PARTNER_FORM + "==" + PARTNER_RECORD;
    }



}