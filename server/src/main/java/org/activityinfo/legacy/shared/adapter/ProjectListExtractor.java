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
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.legacy.shared.model.ProjectDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

/**
 * @author yuriyz on 7/29/14.
 */
public class ProjectListExtractor implements Function<SchemaDTO, List<ProjectDTO>> {

    private final Predicate<ResourceId> formClassCriteria;

    public ProjectListExtractor(Criteria criteria) {
        this.formClassCriteria = CriteriaEvaluation.evaluatePartiallyOnClassId(criteria);
    }

    @Override
    public List<ProjectDTO> apply(SchemaDTO input) {
        List<ProjectDTO> results = Lists.newArrayList();
        for (UserDatabaseDTO db : input.getDatabases()) {
            ResourceId formClassId = CuidAdapter.projectFormClass(db.getId());
            if (formClassCriteria.apply(formClassId)) {
                results.addAll(db.getProjects());
            }
        }
        return results;
    }
}
