package org.activityinfo.server.command.handler;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.legacy.shared.command.UpdateFormInstance;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.server.command.handler.json.JsonHelper;
import org.activityinfo.server.database.hibernate.entity.FormInstanceEntity;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.persistence.EntityManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by yuriy on 3/1/2015.
 */
public class UpdateFormInstanceHandler implements CommandHandler<UpdateFormInstance> {

    private static final Logger LOGGER = Logger.getLogger(UpdateFormInstanceHandler.class.getName());

    private final Provider<EntityManager> entityManager;

    @Inject
    public UpdateFormInstanceHandler(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public CommandResult execute(UpdateFormInstance cmd, User user) throws CommandException {

        assertNotSiteOrLocation(cmd);

        FormInstance formInstance = validateFormInstance(cmd.getJson());

        FormInstanceEntity entity = new FormInstanceEntity();

        entity.setId(formInstance.getId().asString());
        entity.setClassId(formInstance.getClassId().asString());
        entity.setOwnerId(formInstance.getOwnerId() != null ? formInstance.getOwnerId().asString() : null);

        JsonHelper.updateWithJson(entity, cmd.getJson());

        if (!exists(entity.getId())) {
            entityManager.get().persist(entity);
        } else {
            entityManager.get().merge(entity);
        }

        return new VoidResult();
    }

    private boolean exists(String formInstanceId) {
        return entityManager.get().find(FormInstanceEntity.class, formInstanceId) != null;
    }

    private FormInstance validateFormInstance(String json) {
        try {
            Resource resource = Resources.fromJson(json);
            return FormInstance.fromResource(resource);
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
