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
package org.activityinfo.server.command.handler;

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.AddTarget;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.legacy.shared.model.TargetDTO;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.server.database.hibernate.entity.*;
import org.activityinfo.store.spi.DatabaseProvider;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.logging.Logger;

public class AddTargetHandler implements CommandHandler<AddTarget> {

    private static final Logger LOGGER = Logger.getLogger(AddTargetHandler.class.getName());

    private final EntityManager em;
    private final DatabaseProvider databaseProvider;

    @Inject
    public AddTargetHandler(EntityManager em, DatabaseProvider databaseProvider) {
        this.em = em;
        this.databaseProvider = databaseProvider;
    }

    @Override
    public CommandResult execute(AddTarget cmd, User user) {

        TargetDTO form = cmd.getTarget();
        Database db = em.find(Database.class, cmd.getDatabaseId());
        UserDatabaseMeta databaseMeta = databaseProvider.getDatabaseMetadata(
                CuidAdapter.databaseId(db.getId()),
                user.getId());

        assertDesignPrivileges(databaseMeta);
        
        Partner partner = null;
        if (form.get("partnerId") != null) {
            partner = em.find(Partner.class, form.get("partnerId"));
        } else if (form.getPartner() != null) {
            partner = em.find(Partner.class, form.getPartner().getId());
        }

        Project project = null;
        if (form.get("projectId") != null) {
            project = em.find(Project.class, form.get("projectId"));
        } else if (form.getProject() != null) {
            project = em.find(Project.class, form.getProject().getId());
        }
        
        Target target = new Target();
        target.setName(form.getName());
        target.setDatabase(db);
        target.setPartner(partner);
        target.setProject(project);
        target.setDate1(form.getFromDate().atMidnightInMyTimezone());
        target.setDate2(form.getToDate().atMidnightInMyTimezone());

        db.setLastSchemaUpdate(new Date());

        em.persist(target);
        em.persist(db);

        db.getTargets().add(target);

        if (project != null) {
            project.getTargets().add(target);
        }
        if (partner != null) {
            partner.getTargets().add(target);
        }

        return new CreateResult(target.getId());
    }

    private void assertDesignPrivileges(UserDatabaseMeta databaseMeta) {
        if (!PermissionOracle.canDesign(databaseMeta)) {
            LOGGER.severe(String.format("User %d does not have design privileges on database %d",
                    databaseMeta.getUserId(),
                    databaseMeta.getLegacyDatabaseId()));
            throw new IllegalAccessCommandException();
        }
    }
}