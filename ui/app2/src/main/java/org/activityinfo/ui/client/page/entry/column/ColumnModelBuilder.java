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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.ui.client.dispatch.type.IndicatorNumberFormat;
import org.activityinfo.ui.client.page.common.columns.LocalDateColumn;
import org.activityinfo.ui.client.page.common.columns.ReadTextColumn;

import java.util.List;

/**
 * Builder class for constructing a ColumnModel for site grids
 */
public class ColumnModelBuilder {

    private final String warningStyle = "color:tomato; font-size:20px; font-weight:bold";

    private List<ColumnConfig> columns = Lists.newArrayList();

    public ColumnModelBuilder addActivityColumn(final UserDatabaseDTO database) {
        ColumnConfig config = new ColumnConfig("activityId", I18N.CONSTANTS.activity(), 100);
        config.setToolTip(I18N.CONSTANTS.activity());
        config.setRenderer(new GridCellRenderer<SiteDTO>() {

            @Override
            public SafeHtml render(SiteDTO model,
                                 String property,
                                 ColumnData config,
                                 int rowIndex,
                                 int colIndex,
                                 ListStore<SiteDTO> store,
                                 Grid<SiteDTO> grid) {

                ActivityDTO activity = database.getActivityById(model.getActivityId());
                return activity == null ?
                        SafeHtmlUtils.EMPTY_SAFE_HTML :
                        SafeHtmlUtils.fromString(activity.getName());
            }
        });
        columns.add(config);
        return this;
    }

    public ColumnModelBuilder addActivityColumn(final SchemaDTO schema) {
        ColumnConfig config = new ColumnConfig("activityId", I18N.CONSTANTS.activity(), 100);
        config.setToolTip(I18N.CONSTANTS.activity());
        config.setRenderer(new GridCellRenderer<SiteDTO>() {

            @Override
            public SafeHtml render(SiteDTO model,
                                 String property,
                                 ColumnData config,
                                 int rowIndex,
                                 int colIndex,
                                 ListStore<SiteDTO> store,
                                 Grid<SiteDTO> grid) {

                ActivityDTO activity = schema.getActivityById(model.getActivityId());
                return activity == null ? SafeHtmlUtils.EMPTY_SAFE_HTML : SafeHtmlUtils.fromString(activity.getName());
            }
        });
        columns.add(config);
        return this;
    }

    public ColumnModelBuilder addDatabaseColumn(final SchemaDTO schema) {
        ColumnConfig config = new ColumnConfig("activityId", I18N.CONSTANTS.database(), 100);
        config.setToolTip(I18N.CONSTANTS.database());
        config.setRenderer(new GridCellRenderer<SiteDTO>() {

            @Override
            public SafeHtml render(SiteDTO model,
                                 String property,
                                 ColumnData config,
                                 int rowIndex,
                                 int colIndex,
                                 ListStore<SiteDTO> store,
                                 Grid<SiteDTO> grid) {

                ActivityDTO activity = schema.getActivityById(model.getActivityId());
                return activity == null ?
                        SafeHtmlUtils.EMPTY_SAFE_HTML :
                        SafeHtmlUtils.fromString(activity.getDatabaseName());
            }
        });
        columns.add(config);
        return this;
    }

    public ColumnModel build() {
        return new ColumnModel(columns);
    }

    protected ColumnModelBuilder maybeAddProjectColumn(ActivityFormDTO activity) {
        if (!activity.getProjects().isEmpty()) {
            addProjectColumn();
        }
        return this;
    }

    public void addProjectColumn() {
        columns.add(new ReadTextColumn("project", I18N.CONSTANTS.project(), 100));
    }

    private void addLockOrLinkColumn(LockedPeriodSet lockSet) {
        ColumnConfig lockedOrLinkColumn = new ColumnConfig("x", "", 28);
        lockedOrLinkColumn.setRenderer(new LockedOrLinkColumnRenderer(lockSet));
        lockedOrLinkColumn.setSortable(false);
        lockedOrLinkColumn.setMenuDisabled(true);
        columns.add(lockedOrLinkColumn);
    }

    public ColumnModelBuilder maybeAddLockOrLinkColumn(final ActivityFormDTO activity) {
        addLockOrLinkColumn(activity.getLockedPeriodSet());
        return this;
    }

    public ColumnModelBuilder maybeAddLockOrLinkColumn(final SchemaDTO schemaDTO) {
        addLockOrLinkColumn(new LockedPeriodSet(schemaDTO));
        return this;
    }

    public ColumnModelBuilder maybeAddLockOrLinkColumn(final UserDatabaseDTO userdatabase) {
        addLockOrLinkColumn(new LockedPeriodSet(userdatabase));
        return this;
    }

    public ColumnConfig createIndicatorColumn(IndicatorDTO indicator, String header) {

        NumberField indicatorField = new NumberField();
        indicatorField.getPropertyEditor().setFormat(IndicatorNumberFormat.INSTANCE);

        ColumnConfig indicatorColumn = new ColumnConfig(indicator.getPropertyName(), header, 50);

        indicatorColumn.setToolTip(indicator.getName());

        indicatorColumn.setNumberFormat(IndicatorNumberFormat.INSTANCE);
        indicatorColumn.setEditor(new CellEditor(indicatorField));
        indicatorColumn.setAlignment(Style.HorizontalAlignment.RIGHT);

        if (indicator.getType() == FieldTypeClass.QUANTITY) {
            // For SUM indicators, don't show ZEROs in the Grid
            // (it looks better if we don't)
            if (indicator.getAggregation() == IndicatorDTO.AGGREGATE_SUM) {
                indicatorColumn.setRenderer(new GridCellRenderer() {
                    @Override
                    public SafeHtml render(ModelData model,
                                         String property,
                                         ColumnData config,
                                         int rowIndex,
                                         int colIndex,
                                         ListStore listStore,
                                         Grid grid) {
                        Object value = model.get(property);
                        if (value instanceof Double && (Double) value != 0) {
                            return SafeHtmlUtils.fromTrustedString(
                                    IndicatorNumberFormat.INSTANCE.format((Double) value));
                        } else {
                            return SafeHtmlUtils.EMPTY_SAFE_HTML;
                        }
                    }
                });
            } else if (indicator.getAggregation() == IndicatorDTO.AGGREGATE_SITE_COUNT) {
                indicatorColumn.setRenderer(new GridCellRenderer() {
                    @Override
                    public SafeHtml render(ModelData model,
                                         String property,
                                         ColumnData config,
                                         int rowIndex,
                                         int colIndex,
                                         ListStore listStore,
                                         Grid grid) {

                        return SafeHtmlUtils.fromSafeConstant("1"); // the value of a site count indicator a single site is always 1
                    }
                });
            }
        } else if (indicator.getType() == FieldTypeClass.FREE_TEXT || indicator.getType() == FieldTypeClass.NARRATIVE) {
            indicatorColumn.setRenderer(new GridCellRenderer() {
                @Override
                public SafeHtml render(ModelData model,
                                     String property,
                                     ColumnData config,
                                     int rowIndex,
                                     int colIndex,
                                     ListStore listStore,
                                     Grid grid) {

                    Object value = model.get(property);
                    if(value instanceof String) {
                        return SafeHtmlUtils.fromString((String) value);
                    } else {
                        return SafeHtmlUtils.EMPTY_SAFE_HTML;
                    }
                }
            });
        }
        return indicatorColumn;
    }

    public ColumnModelBuilder addIndicatorColumn(IndicatorDTO indicator, String header) {
        columns.add(createIndicatorColumn(indicator, header));
        return this;
    }

    public ColumnModelBuilder maybeAddTwoLineLocationColumn(ActivityFormDTO activity) {
        if (activity.getLocationType().getBoundAdminLevelId() == null) {
            ReadTextColumn column = new ReadTextColumn("locationName", activity.getLocationType().getName(), 100);
            column.setRenderer(new LocationColumnRenderer());
            columns.add(column);
        }
        return this;
    }

    public ColumnModelBuilder maybeAddSingleLineLocationColumn(ActivityFormDTO activity) {
        if (activity.getLocationType().getBoundAdminLevelId() == null) {
            ReadTextColumn column = new ReadTextColumn("locationName", activity.getLocationType().getName(), 100);
            columns.add(column);
        }
        return this;
    }

    public ColumnModelBuilder addLocationColumn() {
        ReadTextColumn column = new ReadTextColumn("locationName", I18N.CONSTANTS.location(), 100);
        columns.add(column);
        return this;
    }

    public ColumnModelBuilder addAdminLevelColumns(ActivityFormDTO activity) {
        return addAdminLevelColumns(activity.getAdminLevels());
    }

    public ColumnModelBuilder addSingleAdminColumn(ActivityFormDTO activity) {
        ColumnConfig admin = new ColumnConfig("admin", I18N.CONSTANTS.location(), 100);
        admin.setToolTip(I18N.CONSTANTS.location());
        admin.setRenderer(new AdminColumnRenderer(activity.getAdminLevels()));
        columns.add(admin);
        return this;
    }

    public ColumnModelBuilder addAdminLevelColumns(List<AdminLevelDTO> adminLevels) {
        for (AdminLevelDTO level : adminLevels) {
            ColumnConfig column = new ColumnConfig(AdminLevelDTO.getPropertyName(level.getId()), level.getName(), 100);
            column.setToolTip(level.getName());
            columns.add(column);
        }

        return this;
    }

    public ColumnModelBuilder addAdminLevelColumns(UserDatabaseDTO database) {
        return addAdminLevelColumns(database.getCountry().getAdminLevels());

    }

    public ColumnModelBuilder maybeAddPartnerColumn(ActivityFormDTO activity) {
        addPartnerColumn();
        return this;
    }

    public ColumnModelBuilder maybeAddPartnerColumn(UserDatabaseDTO activity) {
        addPartnerColumn();
        return this;
    }

    public ColumnModelBuilder addPartnerColumn() {
        ColumnConfig column = new ColumnConfig("partner", I18N.CONSTANTS.partner(), 100);
        column.setToolTip(I18N.CONSTANTS.partner());
        columns.add(column);
        return this;
    }

    public ColumnModelBuilder maybeAddDateColumn(ActivityFormDTO activity) {
        if (activity.getReportingFrequency() == ActivityFormDTO.REPORT_ONCE) {
            columns.add(new LocalDateColumn("date2", I18N.CONSTANTS.endDate(), 100));
        }
        return this;
    }

    public ColumnModelBuilder addMapColumn() {
        ColumnConfig mapColumn = new ColumnConfig("x", "", 25);
        mapColumn.setRenderer(new GridCellRenderer<ModelData>() {
            @Override
            public SafeHtml render(ModelData model,
                                 String property,
                                 ColumnData config,
                                 int rowIndex,
                                 int colIndex,
                                 ListStore listStore,
                                 Grid grid) {
                if (model instanceof SiteDTO) {
                    SiteDTO siteModel = (SiteDTO) model;
                    if (siteModel.hasCoords()) {
                        return SafeHtmlUtils.fromSafeConstant("<div class='mapped'>&nbsp;&nbsp;</div>");
                    } else {
                        return SafeHtmlUtils.fromSafeConstant("<div class='unmapped'>&nbsp;&nbsp;</div>");
                    }
                }
                return SafeHtmlUtils.fromSafeConstant("&nbsp;");
            }
        });
        columns.add(mapColumn);
        return this;
    }

    public ColumnModelBuilder maybeAddKeyIndicatorColumns(ActivityFormDTO activity) {
        // Only add indicators that have a queries heading
        if (activity.getReportingFrequency() == ActivityFormDTO.REPORT_ONCE) {
            for (IndicatorDTO indicator : activity.getIndicators()) {
                if (indicator.getListHeader() != null && !indicator.getListHeader().isEmpty()) {
                    columns.add(createIndicatorColumn(indicator, indicator.getListHeader()));
                }
            }
        }
        return this;
    }

    public ColumnModelBuilder addTreeNameColumn() {
        ColumnConfig name = new ColumnConfig("name", I18N.CONSTANTS.location(), 200);
        name.setToolTip(I18N.CONSTANTS.location());
        name.setRenderer(new TreeGridCellRenderer<ModelData>() {

            @Override
            public SafeHtml render(ModelData model,
                                   String property,
                                   ColumnData config,
                                   int rowIndex,
                                   int colIndex,
                                   ListStore<ModelData> store,
                                   Grid<ModelData> grid) {

                return super.render(model, propertyName(model), config, rowIndex, colIndex, store, grid);
            }

            private String propertyName(ModelData model) {
                if (model instanceof SiteDTO) {
                    return "locationName";
                } else {
                    return "name";
                }
            }

        });
        columns.add(name);

        return this;
    }

    public ColumnModelBuilder maybeAddProjectColumn(UserDatabaseDTO database) {
        if (database.getProjects().size() > 1) {
            addProjectColumn();
        }
        return this;
    }

    public ColumnModelBuilder addDeletedLocationWarning() {
        ColumnConfig deletedWarning = new ColumnConfig("deleted", "", 25);
        deletedWarning.setRenderer(new TreeGridCellRenderer<ModelData>() {

            @Override
            public SafeHtml render(ModelData model,
                                   String property,
                                   ColumnData config,
                                   int rowIndex,
                                   int colIndex,
                                   ListStore<ModelData> store,
                                   Grid<ModelData> grid) {
                if (model instanceof SiteDTO) {
                    String workflowStatus = ((SiteDTO) model).getLocation().getWorkflowStatusId();
                    if (!Strings.isNullOrEmpty(workflowStatus) && workflowStatus.equals(LocationDTO.REJECTED)) {
                        return SafeHtmlUtils.fromSafeConstant("<div style='" + warningStyle + "' title='" + I18N.CONSTANTS.deletedLocation() + "'>&nbsp;!&nbsp;</div>");
                    }
                }
                return SafeHtmlUtils.fromSafeConstant("&nbsp;");
            }
        });

        columns.add(deletedWarning);
        return this;
    }

}
