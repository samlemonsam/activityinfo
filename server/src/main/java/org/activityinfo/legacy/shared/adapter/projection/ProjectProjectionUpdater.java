package org.activityinfo.legacy.shared.adapter.projection;
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

import org.activityinfo.core.shared.Projection;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.legacy.shared.model.ProjectDTO;
import org.activityinfo.model.type.primitive.TextValue;

/**
 * @author yuriyz on 4/23/14.
 */
public class ProjectProjectionUpdater<T> implements ProjectionUpdater<ProjectDTO> {

    private FieldPath path;
    private int fieldIndex;

    ProjectProjectionUpdater(FieldPath path, int fieldIndex) {
        this.path = path;
        this.fieldIndex = fieldIndex;
    }

    @Override
    public void update(Projection projection, ProjectDTO dto) {
        if (dto == null) {
            return;
        }

        switch (fieldIndex) {
            case CuidAdapter.NAME_FIELD:
                projection.setValue(path, TextValue.valueOf(dto.getName()));
                break;
        }
    }
}
