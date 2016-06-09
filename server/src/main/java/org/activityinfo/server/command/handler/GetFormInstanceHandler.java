package org.activityinfo.server.command.handler;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.GetFormInstance;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.FormInstanceListResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.hrd.HrdCatalog;
import org.activityinfo.store.hrd.HrdCollection;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;


public class GetFormInstanceHandler implements CommandHandler<GetFormInstance> {

    private Provider<EntityManager> entityManager;
    private HrdCatalog catalog = new HrdCatalog();

    @Inject
    public GetFormInstanceHandler(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public CommandResult execute(GetFormInstance cmd, User user) throws CommandException {

        if(cmd.getInstanceIds() != null & cmd.getInstanceIds().size() == 1) {
            return fetchById(cmd.getInstanceIds().get(0));
        }
        
        if(cmd.getOwnerIds() != null && cmd.getOwnerIds().size() == 1) {
            ResourceId collectionId = ResourceId.valueOf(cmd.getClassId());
            ResourceId parentId = ResourceId.valueOf(Iterables.getOnlyElement(cmd.getOwnerIds()));
            return fetchByParent(collectionId, parentId);
        }
        
        if(cmd.getType() == GetFormInstance.Type.CLASS) {
            return fetchByClass(ResourceId.valueOf(cmd.getClassId()));
        }
        
        throw new UnsupportedOperationException();

    }

    private CommandResult fetchById(String id) {
        ResourceId resourceId = ResourceId.valueOf(id);
        Optional<ResourceCollection> collection = catalog.lookupCollection(resourceId);
        if(!collection.isPresent()) {
            throw new IllegalArgumentException("No such resource: " + id);
        }

        FormInstance instance;
        HrdCollection hrdCollection = (HrdCollection) collection.get();
        try {
            instance = hrdCollection.getSubmission(resourceId);
        } catch (EntityNotFoundException e) {
            return emptyResult();
        }
        return buildResult(Collections.singleton(instance));
    }


    private CommandResult fetchByParent(ResourceId collectionId, ResourceId parentId) {
        Optional<ResourceCollection> collection = catalog.getCollection(collectionId);
        if(!collection.isPresent()) {
            return emptyResult();
        }
        
        HrdCollection hrdCollection = (HrdCollection) collection.get();
        Iterable<FormInstance> instances = hrdCollection.getSubmissionsOfParent(parentId);
        
        return buildResult(instances);
    }


    private CommandResult fetchByClass(ResourceId collectionId) {
        Optional<ResourceCollection> collection = catalog.getCollection(collectionId);
        if(!collection.isPresent()) {
            return emptyResult();
        }

        HrdCollection hrdCollection = (HrdCollection) collection.get();
        Iterable<FormInstance> instances = hrdCollection.getSubmissions();

        return buildResult(instances);
    }


    private CommandResult emptyResult() {
        return buildResult(Collections.<FormInstance>emptySet());
    }

    private CommandResult buildResult(Iterable<FormInstance> instances) {
        List<String> jsonList = Lists.newArrayList();
        for (FormInstance instance : instances) {
            jsonList.add(Resources.toJson(instance.asResource()));
        }
        return new FormInstanceListResult(jsonList);
    }

}
