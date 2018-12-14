/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.server.command;

import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.PivotSites.PivotResult;
import org.activityinfo.legacy.shared.command.result.SiteResult;
import org.activityinfo.legacy.shared.model.ProjectDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;


@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class ProjectTest extends CommandTestCase {

    @Test
    public void deleteProject() {


        setUser(1);

        long originalDatabaseVersion = lookupDbVersion(1);

        int projectId = 2;

        execute(new Delete("Project", projectId));

        SchemaDTO schema = execute(new GetSchema());
        assertThat(schema.getProjectById(projectId), nullValue());

        // make sure it's gone from sites
        Filter filter = new Filter();
        filter.addRestriction(DimensionType.Activity, 1);
        filter.addRestriction(DimensionType.Site, 3);
        SiteResult sites = execute(new GetSites(filter));

        assertThat(sites.getData().get(0).getProject(), is(nullValue()));

        // and doesn't show up in pivoting...
        PivotSites pivot = new PivotSites();
        Dimension projectDimension = new Dimension(DimensionType.Project);
        pivot.setDimensions(projectDimension);
        pivot.setFilter(filter);

        PivotResult buckets = execute(pivot);
        assertThat(buckets.getBuckets().size(), equalTo(1));
        assertThat(buckets.getBuckets().get(0).getCategory(projectDimension), is(nullValue()));

        // make sure the version number of the database is updated
        assertThat(lookupDbVersion(1), not(equalTo(originalDatabaseVersion)));
    }


    @Test
    public void updateProject() {
        setUser(1);

        SchemaDTO schema = execute(new GetSchema());

        ProjectDTO project = schema.getProjectById(2);
        project.setName("RRMP II");
        project.setDescription("RRMP The Next Generation");

        execute(UpdateEntity.update(project, "name", "description"));

        schema = execute(new GetSchema());

        assertThat(schema.getProjectById(2).getName(), equalTo("RRMP II"));
        assertThat(schema.getProjectById(2).getDescription(), equalTo("RRMP The Next Generation"));

        project.setName("RRMP III");
        project.setDescription(null);

        execute(UpdateEntity.update(project, "name", "description"));
    }


    @Test(expected = Exception.class)
    public void constraintViolation() {
        setUser(1);

        SchemaDTO schema = execute(new GetSchema());

        ProjectDTO project = schema.getProjectById(2);
        project.setName(null);

        execute(UpdateEntity.update(project,"name"));
    }

    private long lookupDbVersion(int dbId) {
        return em.find(Database.class, dbId).getVersion();
    }
}
