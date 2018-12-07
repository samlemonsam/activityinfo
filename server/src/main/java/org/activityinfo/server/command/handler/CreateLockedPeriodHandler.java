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
import org.activityinfo.legacy.shared.command.CreateLockedPeriod;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.legacy.shared.model.LockedPeriodDTO;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.hibernate.entity.*;
import org.activityinfo.store.spi.DatabaseProvider;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;

public class CreateLockedPeriodHandler implements CommandHandler<CreateLockedPeriod> {

    private static final Logger LOGGER = Logger.getLogger(CreateLockedPeriodHandler.class.getName());

    private EntityManager em;
    private DatabaseProvider databaseProvider;

    @Inject
    public CreateLockedPeriodHandler(EntityManager em, DatabaseProvider databaseProvider) {
        this.em = em;
        this.databaseProvider = databaseProvider;
    }

    @Override
    public CommandResult execute(CreateLockedPeriod cmd, User user) {

        Activity activity;
        Database database;
        Project project;
        Folder folder;

        LockedPeriod lockedPeriod = new LockedPeriod();
        LockedPeriodDTO lockedPeriodDTO = cmd.getLockedPeriod();
        lockedPeriod.setFromDate(lockedPeriodDTO.getFromDate().atMidnightInMyTimezone());
        lockedPeriod.setToDate(lockedPeriodDTO.getToDate().atMidnightInMyTimezone());
        lockedPeriod.setName(lockedPeriodDTO.getName());
        lockedPeriod.setEnabled(lockedPeriodDTO.isEnabled());

        int databaseId;
        Optional<UserDatabaseMeta> databaseMeta;
        if (cmd.getDatabaseId() != 0) {
            database = em.find(Database.class, cmd.getDatabaseId());
            lockedPeriod.setDatabase(database);
            databaseId = database.getId();
            databaseMeta = getDatabaseMeta(databaseId, user.getId());

            assertLockRecordsRights(databaseMeta);

        } else if (cmd.getProjectId() != 0) {
            project = em.find(Project.class, cmd.getProjectId());
            lockedPeriod.setProject(project);
            lockedPeriod.setDatabase(project.getDatabase());
            databaseId = project.getDatabase().getId();
            databaseMeta = getDatabaseMeta(databaseId, user.getId());

            assertLockRecordsRights(databaseMeta);

        } else if (cmd.getActivityId() != 0) {
            activity = em.find(Activity.class, cmd.getActivityId());
            lockedPeriod.setActivity(activity);
            lockedPeriod.setDatabase(activity.getDatabase());
            databaseId = activity.getDatabase().getId();
            databaseMeta = getDatabaseMeta(databaseId, user.getId());

            assertLockRecordsRights(activity.getFormId(), databaseMeta);

        } else if (cmd.getFolderId() != 0) {
            folder = em.find(Folder.class, cmd.getFolderId());
            lockedPeriod.setFolder(folder);
            lockedPeriod.setDatabase(folder.getDatabase());
            databaseId = folder.getDatabase().getId();
            databaseMeta = getDatabaseMeta(databaseId, user.getId());

            assertLockRecordsRights(CuidAdapter.folderId(folder.getId()), databaseMeta);

        } else {
            throw new CommandException("One of the following must be provided: userDatabaseId, projectId, activityId, folderId");
        }

        Database db = em.find(Database.class, databaseId);

        em.persist(lockedPeriod);

        db.setLastSchemaUpdate(new Date());
        em.persist(db);

        return new CreateResult(lockedPeriod.getId());
    }

    private Optional<UserDatabaseMeta> getDatabaseMeta(int databaseId, int userId) {
        return databaseProvider.getDatabaseMetadata(
                CuidAdapter.databaseId(databaseId),
                userId);
    }

    void assertLockRecordsRights(Optional<UserDatabaseMeta> userDatabaseMeta) {
        if (!userDatabaseMeta.isPresent()) {
            throw new IllegalArgumentException("Database must exist");
        }
        assertLockRecordsRights(userDatabaseMeta.get().getDatabaseId(), userDatabaseMeta);
    }

    void assertLockRecordsRights(ResourceId resourceId, Optional<UserDatabaseMeta> userDatabaseMeta) {
        if (!userDatabaseMeta.isPresent()) {
            throw new IllegalArgumentException("Database must exist");
        }
        if (!PermissionOracle.canLockRecords(resourceId, userDatabaseMeta.get())) {
            throw new IllegalAccessCommandException();
        }
    }

}
