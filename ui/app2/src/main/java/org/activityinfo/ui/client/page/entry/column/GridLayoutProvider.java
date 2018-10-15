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
package org.activityinfo.ui.client.page.entry.column;

import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.page.entry.grouping.AdminGroupingModel;
import org.activityinfo.ui.client.page.entry.grouping.GroupingModel;
import org.activityinfo.ui.client.page.entry.grouping.NullGroupingModel;
import org.activityinfo.ui.client.page.entry.grouping.TimeGroupingModel;

public class GridLayoutProvider {

    private final Dispatcher dispatcher;

    public GridLayoutProvider(Dispatcher dispatcher) {
        super();
        this.dispatcher = dispatcher;
    }

    public void fetch(final Filter filter,
                      final GroupingModel grouping,
                      final AsyncCallback<GridLayout> callback) {

        if (filter.isDimensionRestrictedToSingleCategory(DimensionType.Activity)) {
            final int activityId = filter.getRestrictedCategory(DimensionType.Activity);
            dispatcher.execute(new GetActivityForm(activityId)).then(new AsyncCallback<ActivityFormDTO>() {
                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(ActivityFormDTO activity) {
                    dispatcher.execute(new GetSchema()).then(new AsyncCallback<SchemaDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            callback.onFailure(caught);
                        }

                        @Override
                        public void onSuccess(SchemaDTO schema) {
                            callback.onSuccess(forSingleActivity(grouping,
                                    schema.getDatabaseById(activity.getDatabaseId()),
                                    activity));
                        }
                    });
                }
            });

        } else {
            dispatcher.execute(new GetSchema()).then(new AsyncCallback<SchemaDTO>() {
                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(SchemaDTO result) {
                    int databaseId = filter.getRestrictedCategory(DimensionType.Database);
                    UserDatabaseDTO singleDatabase = result.getDatabaseById(filter.getRestrictedCategory(DimensionType.Database));
                    callback.onSuccess(forSingleDatabase(grouping, singleDatabase));
                }
            });
        }
    }

    private GridLayout forSingleActivity(GroupingModel grouping, UserDatabaseDTO database, ActivityFormDTO activity) {

        String heading = activity.getDatabaseName() + " - " + activity.getName();

        if(database.isSuspended()) {
            return GridLayout.suspended(heading, database.getAmOwner());

        } else if(activity.getClassicView()) {
            return GridLayout.classic(heading, activity, columnsForSingleActivity(grouping, database, activity));

        } else {
            return GridLayout.redirect(heading, activity.getResourceId());
        }
    }

    /**
     * Builds a grid model for a single classic activity
     *
     */
    private ColumnModel columnsForSingleActivity(GroupingModel grouping, UserDatabaseDTO database, ActivityFormDTO activity) {
        if (grouping == NullGroupingModel.INSTANCE) {
            return new ColumnModelBuilder().addMapColumn()
                                           .addDeletedLocationWarning()
                                           .maybeAddLockOrLinkColumn(database)
                                           .maybeAddDateColumn(activity)
                                           .addPartnerColumn()
                                           .maybeAddProjectColumn(activity)
                                           .maybeAddKeyIndicatorColumns(activity)
                                           .maybeAddTwoLineLocationColumn(activity)
                                           .addAdminLevelColumns(activity)
                                           .build();
        } else if (grouping instanceof AdminGroupingModel) {

            return new ColumnModelBuilder().maybeAddLockOrLinkColumn(database)
                                           .addTreeNameColumn()
                                           .maybeAddDateColumn(activity)
                                           .addPartnerColumn()
                                           .maybeAddProjectColumn(activity)
                                           .build();
        } else if (grouping instanceof TimeGroupingModel) {

            return new ColumnModelBuilder().addDeletedLocationWarning()
                                           .maybeAddLockOrLinkColumn(database)
                                           .addTreeNameColumn()
                                           .maybeAddDateColumn(activity)
                                           .addPartnerColumn()
                                           .maybeAddProjectColumn(activity)
                                           .maybeAddSingleLineLocationColumn(activity)
                                           .addAdminLevelColumns(activity)
                                           .build();
        } else {
            throw new IllegalArgumentException(grouping.toString());
        }
    }

    private GridLayout forSingleDatabase(GroupingModel grouping, UserDatabaseDTO database) {
        if(database.isSuspended()) {
            return GridLayout.suspended(database.getName(), database.getAmOwner());
        } else {
            return GridLayout.classic(database.getName(), columnsForSingleDatabase(grouping, database));
        }
    }

    private ColumnModel columnsForSingleDatabase(GroupingModel grouping, UserDatabaseDTO database) {
        if (grouping == NullGroupingModel.INSTANCE) {
            return new ColumnModelBuilder().addMapColumn()
                                           .addDeletedLocationWarning()
                                           .maybeAddLockOrLinkColumn(database)
                                           .addActivityColumn(database)
                                           .addLocationColumn()
                                           .addPartnerColumn()
                                           .maybeAddProjectColumn(database)
                                           .addAdminLevelColumns(database)
                                           .build();

        } else if (grouping instanceof AdminGroupingModel) {

            return new ColumnModelBuilder().addTreeNameColumn()
                                           .maybeAddLockOrLinkColumn(database)
                                           .addActivityColumn(database)
                                           .addPartnerColumn()
                                           .maybeAddProjectColumn(database)
                                           .build();

        } else if (grouping instanceof TimeGroupingModel) {

            return new ColumnModelBuilder().addDeletedLocationWarning()
                                           .addTreeNameColumn()
                                           .maybeAddLockOrLinkColumn(database)
                                           .addActivityColumn(database)
                                           .addPartnerColumn()
                                           .maybeAddProjectColumn(database)
                                           .addLocationColumn()
                                           .addAdminLevelColumns(database)
                                           .build();
        } else {
            throw new IllegalArgumentException(grouping.toString());
        }
    }
}
