package org.activityinfo.legacy.shared.command;
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
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

/**
 * @author yuriyz on 01/25/2016.
 */
public class DeleteFormInstance implements MutatingCommand<VoidResult> {

    private List<String> resourceIds = Lists.newArrayList();

    public DeleteFormInstance() {
    }

    public DeleteFormInstance(List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public DeleteFormInstance setInstanceIdList(List<ResourceId> instanceIdList) {
        resourceIds.clear();
        for (ResourceId id : instanceIdList) {
            resourceIds.add(id.asString());
        }
        return this;
    }
}
