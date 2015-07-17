package org.activityinfo.ui.client.component.report.editor.chart;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.ActivityFormResults;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.AdminLevelDTO;
import org.activityinfo.legacy.shared.model.AttributeGroupDTO;
import org.activityinfo.legacy.shared.reports.model.PivotReportElement;
import org.activityinfo.promise.Promise;
import org.activityinfo.server.database.hibernate.entity.AttributeGroup;
import org.activityinfo.ui.client.component.report.editor.pivotTable.DimensionModel;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Dimensions {

    private final Map<Integer, AttributeGroupDTO> groupMap = Maps.newHashMap();
    private final List<AttributeGroupDTO> groups = Lists.newArrayList();

    private final Map<Integer, AdminLevelDTO> levelMap = Maps.newHashMap();
    private final List<AdminLevelDTO> levels = Lists.newArrayList();

    private List<DimensionModel> levelDimensions;
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

    private static GetAttributeGroupsDimension queryAttributeGroups(Set<Integer> indicators) {
        Filter filter = new Filter();
        filter.addRestriction(DimensionType.Indicator, indicators);

        GetAttributeGroupsDimension query = new GetAttributeGroupsDimension();
        query.setFilter(filter);

        return query;
    }

    private static GetAdminLevels queryAdminLevels(Set<Integer> indicators) {
        GetAdminLevels query = new GetAdminLevels();
        query.setIndicatorIds(indicators);
        return query;
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
