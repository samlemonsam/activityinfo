package org.activityinfo.server.endpoint.rest;

import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.server.command.handler.PermissionOracle;
import org.activityinfo.store.query.impl.ValueVisibilityChecker;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Created by yuriyz on 8/31/2016.
 */
public class UpdateValueVisibilityChecker implements ValueVisibilityChecker {

    private final PermissionOracle permissionOracle;

    public UpdateValueVisibilityChecker(PermissionOracle permissionOracle) {
        this.permissionOracle = permissionOracle;
    }

    @Override
    public void assertVisible(FieldType fieldType, FieldValue value, int userId) {
        try {
            if (fieldType instanceof AttachmentType) {
                AttachmentValue attachmentValue = (AttachmentValue) value;
                permissionOracle.assertEditAllowed(attachmentValue, userId);
            }
        } catch (IllegalAccessCommandException e) {
            throw new WebApplicationException(
                    Response.status(Response.Status.FORBIDDEN)
                            .entity("You do not have permission for this operation.")
                            .build());
        }
    }
}
