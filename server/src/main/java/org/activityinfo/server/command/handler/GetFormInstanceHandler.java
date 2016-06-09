package org.activityinfo.server.command.handler;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.GetFormInstance;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.FormInstanceListResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.command.handler.json.JsonHelper;
import org.activityinfo.server.database.hibernate.entity.FormInstanceEntity;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.hrd.HrdCatalog;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * TODO Revise this class! Implementation must be based on Criteria and Cloud Storage
 *
 * Created by yuriy on 3/1/2015.
 */
public class GetFormInstanceHandler implements CommandHandler<GetFormInstance> {

    private Provider<EntityManager> entityManager;
    private HrdCatalog catalog = new HrdCatalog();

    @Inject
    public GetFormInstanceHandler(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public CommandResult execute(GetFormInstance cmd, User user) throws CommandException {

        Optional<ResourceCollection> collection = catalog.getCollection(ResourceId.valueOf(cmd.getClassId()));
        if(!collection.isPresent()) {
            throw new IllegalArgumentException("No such collection: " + cmd.getClassId());
        }

        FormClass formClass = collection.get().getFormClass();
        
        
        switch (cmd.getType()) {
            case ID:
                return fecthById(cmd);
            case CLASS:
                return fetchByClass(cmd);
            case OWNER:
                return fetchByOwner(cmd);
        }
        throw new CommandException("Unsupported GetFormInstance type:" + cmd.getType());

    }

    private CommandResult fetchByClass(GetFormInstance cmd) {
        
        List<FormInstanceEntity> entities = entityManager.get().createQuery(
                "SELECT d FROM FormInstanceEntity d WHERE formInstanceClassId = :classId")
                .setParameter("classId", cmd.getClassId())
                .getResultList();
        return new FormInstanceListResult(JsonHelper.readJsons(entities));
    }

    private CommandResult fetchByOwner(GetFormInstance cmd) {
        final List<FormInstanceEntity> entities = Lists.newArrayList();

        for (String ownerId : cmd.getOwnerIds()) {
            if (Strings.isNullOrEmpty(cmd.getClassId())) {
                entities.addAll(entityManager.get().createQuery(
                        "SELECT d FROM FormInstanceEntity d WHERE formInstanceOwnerId = :ownerId")
                        .setParameter("ownerId", ownerId)
                        .getResultList());
            } else {
                entities.addAll(entityManager.get().createQuery(
                        "SELECT d FROM FormInstanceEntity d WHERE formInstanceOwnerId = :ownerId AND formInstanceClassId = :classId")
                        .setParameter("ownerId", ownerId)
                        .setParameter("classId", cmd.getClassId())
                        .getResultList());
            }
        }
        return new FormInstanceListResult(JsonHelper.readJsons(entities));
    }

    private CommandResult fecthById(GetFormInstance cmd) {
        List<FormInstanceEntity> entities = fetchEntities(cmd.getInstanceIds());
        return new FormInstanceListResult(JsonHelper.readJsons(entities));
    }

    private List<FormInstanceEntity> fetchEntities(List<String> instanceIds) {
        List<FormInstanceEntity> entities = Lists.newArrayList();
        for (String instanceId : instanceIds) {
            entities.add(entityManager.get().find(FormInstanceEntity.class, instanceId));
        }
        return entities;
    }
    
    
}
