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
package org.activityinfo.ui.client.local.sync;

import com.bedatadriven.rebar.sql.client.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class SchemaMigration {

    private final SqlDatabase database;

    // This should be generated automatically from the liquibase change set logs.
    // This will do until then:

    private static final String[] MIGRATION_DDL = new String[]{"ALTER TABLE Site ADD COLUMN timeEdited REAL",
            "ALTER TABLE Location ADD COLUMN timeEdited REAL",
            "CREATE TABLE IF NOT EXISTS  siteattachment (blobid TEXT, siteid INT, filename TEXT, uploadedBy INT, " +
            "blobSize REAL, contentType TEXT)",
            "ALTER TABLE UserDatabase ADD COLUMN version REAL",
            "ALTER TABLE UserPermission ADD COLUMN version REAL",
            "ALTER TABLE AdminLevel ADD COLUMN polygons INT",
            "ALTER TABLE UserLogin ADD COLUMN emailnotification INT",
            "CREATE INDEX IF NOT EXISTS adminentity_pk on adminentity (adminentityid)",
            "CREATE INDEX IF NOT EXISTS location_link on locationadminlink (locationid)",
            "CREATE INDEX IF NOT EXISTS location_entity on locationadminlink (adminentityid)",
            "ALTER TABLE indicator ADD COLUMN mandatory INT",
            "ALTER TABLE attributeGroup ADD COLUMN mandatory INT",
            "ALTER TABLE adminentity ADD COLUMN deleted INT",
            "ALTER TABLE adminlevel ADD COLUMN deleted INT",
            "ALTER TABLE adminlevel ADD COLUMN version INT",
            "ALTER TABLE adminlevel DROP COLUMN AllowAdd",
            "ALTER TABLE UserLogin DROP COLUMN NewUser",
            "ALTER TABLE UserLogin DROP COLUMN FirstName",
            "ALTER TABLE UserLogin ADD COLUMN organization TEXT",
            "ALTER TABLE UserLogin ADD COLUMN jobtitle TEXT",
            "ALTER TABLE UserLogin ADD COLUMN invitedBy INT",
            "ALTER TABLE UserLogin ADD COLUMN dateCreated REAL",
            "ALTER TABLE Location ADD COLUMN workflowStatusId",
            "ALTER TABLE LocationType ADD COLUMN workflowId",
            "ALTER TABLE LocationType ADD COLUMN databaseId",
            "ALTER TABLE IndicatorValue ADD COLUMN TextValue",
            "ALTER TABLE IndicatorValue ADD COLUMN DateValue",
            "ALTER TABLE IndicatorValue ADD COLUMN BooleanValue",
            "ALTER TABLE Indicator ADD COLUMN type",
            "ALTER TABLE Indicator ADD COLUMN expression",
            "ALTER TABLE Indicator ADD COLUMN calculatedAutomatically",
            "ALTER TABLE Indicator ADD COLUMN nameInExpression",
            "ALTER TABLE AttributeGroup ADD COLUMN defaultValue",
            "ALTER TABLE AttributeGroup ADD COLUMN workflow",
            "UPDATE location SET workflowStatusId='validated' WHERE workflowStatusId is null ",
            "ALTER TABLE indicator ADD COLUMN visible bit(1) NOT null DEFAULT 1",
            "CREATE TABLE IF NOT EXISTS folder (folderId INT, databaseId INT, name TEXT, sortOrder int)",
            "ALTER TABLE activity ADD COLUMN folderId INT",
            "CREATE TABLE IF NOT EXISTS groupassignment (userPermissionId INT, partnerId INT)"
    };

    @Inject
    public SchemaMigration(SqlDatabase database) {
        super();
        this.database = database;
    }

    public void migrate(final AsyncCallback<Void> callback) {
        database.transaction(new SqlTransactionCallback() {

            @Override
            public void begin(SqlTransaction tx) {
                for (String ddl : MIGRATION_DDL) {
                    tx.executeSql(ddl, new SqlResultCallback() {

                        @Override
                        public void onSuccess(SqlTransaction tx, SqlResultSet results) {
                        }

                        @Override
                        public boolean onFailure(SqlException e) {
                            // ignore errors resulting from ddl that has already
                            // been appsavelied
                            return SqlResultCallback.CONTINUE;
                        }

                    });
                }
            }

            @Override
            public void onSuccess() {
                callback.onSuccess(null);
            }

            @Override
            public void onError(SqlException e) {
                callback.onFailure(e);
            }
        });

    }

}
