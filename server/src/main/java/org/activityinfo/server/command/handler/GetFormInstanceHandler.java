package org.activityinfo.server.command.handler;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.GetFormInstance;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.FormInstanceListResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.server.command.handler.json.JsonHelper;
import org.activityinfo.server.database.hibernate.entity.FormInstanceEntity;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by yuriy on 3/1/2015.
 */
public class GetFormInstanceHandler implements CommandHandler<GetFormInstance> {

    private Provider<EntityManager> entityManager;

    @Inject
    public GetFormInstanceHandler(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public CommandResult execute(GetFormInstance cmd, User user) throws CommandException {
        switch (cmd.getType()) {
            case ID:
                return fecthById(cmd);
            case OWNER:
                return fetchByOwner(cmd);
        }
        throw new CommandException("Unsupported GetFormInstance type:" + cmd.getType());

    }

    private CommandResult fetchByOwner(GetFormInstance cmd) {
        List<FormInstanceEntity> entities = entityManager.get().createQuery(
                "SELECT d FROM FormInstanceEntity d WHERE formInstanceOwnerId = :ownerId")
                .setParameter("ownerId", cmd.getOwnerId())
                .getResultList();
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
