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
package org.activityinfo.ui.client.component.report.editor.chart;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.legacy.shared.command.GetActivityForms;
import org.activityinfo.legacy.shared.command.result.ActivityFormResults;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.AdminLevelDTO;
import org.activityinfo.legacy.shared.model.AttributeGroupDTO;
import org.activityinfo.legacy.shared.reports.model.PivotReportElement;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.report.editor.pivotTable.DimensionModel;
import org.activityinfo.ui.client.dispatch.Dispatcher;

import java.util.List;
import java.util.Map;

public class Dimensions {

    private final Map<Integer, AttributeGroupDTO> groupMap = Maps.newHashMap();
    private final List<AttributeGroupDTO> groups = Lists.newArrayList();

    private final Map<Integer, AdminLevelDTO> levelMap = Maps.newHashMap();
    private final List<AdminLevelDTO> levels = Lists.newArrayList();

    private List<DimensionModel> attributeDimensions;

    private Dimensions(List<ActivityFormDTO> forms) {
        for(ActivityFormDTO form : forms) {
            for(AdminLevelDTO level : form.getAdminLevels()) {
                if(levelMap.put(level.getId(), level) == null) {
                    levels.add(level);
                }
            }
            for(AttributeGroupDTO group : form.getAttributeGroups()) {
                if(groupMap.put(group.getId(), group) == null) {
                    groups.add(group);
                }
            }
        }
    }

    public Dimensions() {
    }

    public static Promise<Dimensions> loadDimensions(Dispatcher dispatcher, PivotReportElement model) {

        if(model.getIndicators().isEmpty()) {
            return Promise.resolved(new Dimensions());
        }

        return dispatcher.execute(new GetActivityForms(model.getIndicators())).then(new Function<ActivityFormResults, Dimensions>() {
            @Override
            public Dimensions apply(ActivityFormResults input) {
                return new Dimensions(input.getData());
            }
        });
    }

    public List<DimensionModel> getAdminLevelDimensions() {
        return DimensionModel.adminLevelModels(levels);
    }

    public List<DimensionModel> getAttributeDimensions() {
        return DimensionModel.attributeGroupModels(groups);
    }

    public String getAttributeGroupNameSafe(int attributeGroupId) {
        AttributeGroupDTO group = groupMap.get(attributeGroupId);
        if(group == null) {
            return "";
        }
        return group.getName();
    }

    public AdminLevelDTO getAdminLevelById(int levelId) {
        return levelMap.get(levelId);
    }

    public AttributeGroupDTO getAttributeGroupById(int attributeGroupId) {
        return groupMap.get(attributeGroupId);
    }
}
