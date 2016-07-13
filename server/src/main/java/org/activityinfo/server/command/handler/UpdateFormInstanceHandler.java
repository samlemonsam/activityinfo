package org.activityinfo.server.command.handler;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.legacy.shared.command.UpdateFormInstance;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.server.database.hibernate.entity.Site;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.hrd.HrdCatalog;

import javax.persistence.EntityManager;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by yuriy on 3/1/2015.
 */
public class UpdateFormInstanceHandler implements CommandHandler<UpdateFormInstance> {

    private static final Logger LOGGER = Logger.getLogger(UpdateFormInstanceHandler.class.getName());

    private PermissionOracle permissionOracle;
    private Provider<EntityManager> entityManager;

    @Inject
    public UpdateFormInstanceHandler(PermissionOracle permissionOracle, Provider<EntityManager> entityManager) {
        this.permissionOracle = permissionOracle;
        this.entityManager = entityManager;
    }

    @Override
    public CommandResult execute(UpdateFormInstance cmd, User user) throws CommandException {

        assertNotSiteOrLocation(cmd);

        FormInstance formInstance = validateFormInstance(cmd.getJson());

        // Check permission to edit
        ResourceId parentId = formInstance.getOwnerId();
        if(parentId.getDomain() != CuidAdapter.SITE_DOMAIN) {
            throw new UnsupportedOperationException("Permission check only implemented for subform instances of sites");
        }
        int parentSiteId = CuidAdapter.getLegacyIdFromCuid(parentId);
        Site parentSite = entityManager.get().find(Site.class, parentSiteId);
        permissionOracle.assertEditAllowed(parentSite, user);
        
 
        // Translate the form instance to a ResourceUpdate
        RecordUpdate update = new RecordUpdate();
        update.setResourceId(formInstance.getId());
        update.setParentId(formInstance.getOwnerId());

        for (Map.Entry<ResourceId, FieldValue> entry : formInstance.getFieldValueMap().entrySet()) {
            if(!entry.getKey().equals(ResourceId.valueOf("classId")) &&
               !entry.getKey().equals(ResourceId.valueOf("sort"))) {
                update.set(entry.getKey(), entry.getValue());
            }
        }

        HrdCatalog catalog = new HrdCatalog();
        Optional<ResourceCollection> collection = catalog.getCollection(formInstance.getClassId());
        if(!collection.isPresent()) {
            throw new IllegalStateException("Could not get Resource Collection: " + collection);
        }
        collection.get().update(update);
        
        return new VoidResult();
    }
    
    private FormInstance validateFormInstance(String json) {
        try {
            return FormInstance.fromJson(json);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Invalid FormInstance json: " + e.getMessage(), e);
            throw new CommandException();
        }
    }

    private static void assertNotSiteOrLocation(UpdateFormInstance cmd) {
        if (ResourceId.valueOf(cmd.getFormInstanceId()).getDomain() != ResourceId.GENERATED_ID_DOMAIN) {
            String msg = "Failed to persist instance, id:" + cmd.getFormInstanceId() + " Persists instances only with generated id." +
                    " Please check SitePersister for sites persistence or LocationPersister for location persistence.";

            LOGGER.log(Level.SEVERE, msg);
            throw new CommandException(new UnsupportedOperationException(msg));
        }
    }

}
