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

import com.google.common.base.Function;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.legacy.shared.model.ProjectDTO;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

/**
 * @author yuriyz on 7/29/14.
 */
public class ProjectInstanceAdapter implements Function<ProjectDTO, FormInstance> {

    private final ResourceId classId;

    public ProjectInstanceAdapter(ResourceId formClassId) {
        this.classId = formClassId;
    }

    @Override
    public FormInstance apply(ProjectDTO input) {
        FormInstance instance = new FormInstance(CuidAdapter.cuid(CuidAdapter.PROJECT_DOMAIN, input.getId()), classId);

        instance.set(CuidAdapter.field(classId, CuidAdapter.NAME_FIELD), input.getName());
        return instance;
    }
}
