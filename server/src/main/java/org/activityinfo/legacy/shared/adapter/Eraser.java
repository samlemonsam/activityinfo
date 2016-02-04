package org.activityinfo.legacy.shared.adapter;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.core.shared.workflow.Workflow;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.BatchResult;
import org.activityinfo.legacy.shared.command.result.FormInstanceListResult;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 3/27/14.
 */
public class Eraser {

    private final Dispatcher dispatcher;
    private final Collection<ResourceId> instanceIds;

    public Eraser(Dispatcher dispatcher, Collection<ResourceId> instanceIds) {
        this.dispatcher = dispatcher;
        this.instanceIds = instanceIds;
    }

    public Promise<Void> execute() {

        List<Command> commands = Lists.newArrayList();
        List<ResourceId> instanceWithoutMapping = Lists.newArrayList();

        for (ResourceId instanceId : instanceIds) {

            char domain = instanceId.getDomain();

            if (domain == CuidAdapter.LOCATION_DOMAIN) {

                // it's workaround, instead of deletion we update/mark Location with workflowstatusid=rejected

                Map<String, Object> properties = new HashMap<>();
                properties.put("id", CuidAdapter.getLegacyIdFromCuid(instanceId));
                properties.put("workflowstatusid", Workflow.REJECTED);

                commands.add(new CreateLocation(properties));
            } else if (domain == CuidAdapter.SITE_DOMAIN) {

                commands.add(new DeleteSite(CuidAdapter.getLegacyIdFromCuid(instanceId)));
            } else if (domain == CuidAdapter.PROJECT_DOMAIN) {


                commands.add(new Delete("Project", CuidAdapter.getLegacyIdFromCuid(instanceId)));
            } else if (domain == CuidAdapter.ACTIVITY_DOMAIN) {

                commands.add(new Delete("Activity", CuidAdapter.getLegacyIdFromCuid(instanceId)));
            } else if (domain == CuidAdapter.DATABASE_DOMAIN) {

                commands.add(new Delete("UserDatabase", CuidAdapter.getLegacyIdFromCuid(instanceId)));
            } else if (domain == ResourceId.GENERATED_ID_DOMAIN) {

                instanceWithoutMapping.add(instanceId);
            }
        }

        if (!instanceWithoutMapping.isEmpty()) {
            commands.add(new DeleteFormInstance().setInstanceIdList(instanceWithoutMapping));
        }

        if (commands.isEmpty()) {
            return Promise.rejected(new UnsupportedOperationException());
        }

        final Promise<Void> result = new Promise<>();

        dispatcher.execute(new BatchCommand(commands)).then(new AsyncCallback<BatchResult>() {
            @Override
            public void onFailure(Throwable caught) {
                result.onFailure(caught);
            }

            @Override
            public void onSuccess(BatchResult batchResult) {
                dispatcher.execute(new GetFormInstance().setType(GetFormInstance.Type.OWNER).setOwnerIdList(instanceIds)).then(new AsyncCallback<FormInstanceListResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        result.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(FormInstanceListResult batchResult) {

                        if (batchResult.getFormInstanceList().isEmpty()) {
                            result.onSuccess(null);
                            return;
                        }

                        List<ResourceId> ids = Lists.newArrayList();

                        for (FormInstance instance : batchResult.getFormInstanceList()) {
                            if (instance.getId().getDomain() == ResourceId.GENERATED_ID_DOMAIN) {
                                ids.add(instance.getId());
                            }
                        }

                        dispatcher.execute(new DeleteFormInstance().setInstanceIdList(ids)).then(new AsyncCallback<VoidResult>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                result.onFailure(caught);
                            }

                            @Override
                            public void onSuccess(VoidResult voidResult) {
                                result.onSuccess(null);
                            }
                        });
                    }
                });
            }
        });
        return result;
    }
}
