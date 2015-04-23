package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.command.CreateLocation;
import org.activityinfo.legacy.shared.command.GetAdminEntities;
import org.activityinfo.legacy.shared.command.result.AdminEntityResult;
import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.legacy.shared.model.AdminLevelDTO;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.promise.Promise;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static org.activityinfo.model.legacy.CuidAdapter.*;

/**
 * Updates/creates a location object.
 * <p/>
 * <p>The legacy api expects the location to be denormalized with
 * ALL ancestors, so we have to fetch them before we're able to persist</p>
 */
public class LocationPersister {

    private final Dispatcher dispatcher;
    private final FormInstance instance;
    private Promise<Void> callback;
    private Map<String, Object> properties;
    private Queue<Integer> parents = new LinkedList<>();
    private ResourceId classId;

    public LocationPersister(Dispatcher dispatcher, FormInstance instance) {
        this.dispatcher = dispatcher;
        this.instance = instance;
        this.classId = instance.getClassId();
    }

    public Promise<Void> persist() {
        callback = new Promise<Void>();

        properties = Maps.newHashMap();
        properties.put("id", CuidAdapter.getLegacyIdFromCuid(instance.getId()));
        properties.put("locationTypeId", getLegacyIdFromCuid(classId));
        properties.put("name", unwrapText(instance.get(field(classId, NAME_FIELD))));
        properties.put("axe", unwrapText(instance.get(field(classId, AXE_FIELD))));

        GeoPoint point = (GeoPoint) instance.get(field(classId, GEOMETRY_FIELD));
        if (point != null) {
            properties.put("latitude", point.getLatitude());
            properties.put("longitude", point.getLongitude());
        }

        Set<ResourceId> adminEntities = instance.getReferences(field(classId, ADMIN_FIELD));
        if (adminEntities != null) {
            for (ResourceId adminEntityResourceId : adminEntities) {
                parents.add(getLegacyIdFromCuid(adminEntityResourceId));
            }
        }

        resolveNextParent();
        return callback;
    }

    private Object unwrapText(FieldValue fieldValue) {
        if(fieldValue instanceof TextValue) {
            return fieldValue.toString();
        } else {
            return null;
        }
    }

    private void resolveNextParent() {
        if (parents.isEmpty()) {
            persistLocation();
            return;
        }
        final GetAdminEntities query = new GetAdminEntities();
        query.setEntityId(parents.poll());

        dispatcher.execute(query, new AsyncCallback<AdminEntityResult>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(AdminEntityResult result) {
                if (result.getData().isEmpty()) {
                    callback.onFailure(new IllegalStateException("No entity with id = " + query.getEntityIds()));
                }
                AdminEntityDTO entity = result.getData().get(0);
                properties.put(AdminLevelDTO.getPropertyName(entity.getLevelId()), entity.getId());
                if (entity.getParentId() != null && !parents.contains(entity.getParentId())) {
                    parents.add(entity.getParentId());
                }
                resolveNextParent();
            }
        });
    }

    private void persistLocation() {
        CreateLocation command = new CreateLocation(properties);
        dispatcher.execute(command).then(Functions.<Void>constant(null)).then(callback);
    }


}
