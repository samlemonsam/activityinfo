package org.activityinfo.ui.client.component.form.field.hierarchy;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.criteria.IdCriteria;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.activityinfo.core.shared.application.ApplicationProperties.LABEL_PROPERTY;
import static org.activityinfo.core.shared.application.ApplicationProperties.PARENT_PROPERTY;

class InitialSelection {

    private final Hierarchy hierarchy;
    private final Map<ResourceId, Projection> selection = Maps.newHashMap();

    public InitialSelection(Hierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    public Promise<Void> fetch(ResourceLocator locator, Set<ResourceId> ids) {
        if(ids == null || ids.isEmpty()) {
            return Promise.done();
        } else {
            return fetchLabelAndParentIds(locator, ids);
        }
    }

    private Promise<Void> fetchLabelAndParentIds(final ResourceLocator locator, Set<ResourceId> instanceIds) {
        InstanceQuery query = InstanceQuery
                .select(LABEL_PROPERTY, PARENT_PROPERTY)
                .where(new IdCriteria(null, instanceIds))
                .build();

        return locator.query(query)
                  .join(new Function<List<Projection>, Promise<Void>>() {
                      @Override
                      public Promise<Void> apply(List<Projection> projections) {

                          Set<ResourceId> parents = populateSelection(projections);
                          if (parents.isEmpty()) {
                              return Promise.done();
                          } else {
                              return fetchLabelAndParentIds(locator, parents);
                          }
                      }
                  });
    }

    private Set<ResourceId> populateSelection(List<Projection> projections) {
        Set<ResourceId> parents = Sets.newHashSet();
        for(Projection projection : projections) {
            Level level = hierarchy.getLevel(projection.getRootClassId());
            if(level != null) {
                selection.put(projection.getRootClassId(), projection);
                if(!level.isRoot()) {
                    ResourceId parentId = projection.getReferenceValue(PARENT_PROPERTY)
                                                    .iterator().next();
                    assert parentId != null;
                    parents.add(parentId);
                }
            }
        }
        parents.removeAll(selection.keySet());
        return parents;
    }

    public Map<ResourceId, Projection> getSelection() {
        return selection;
    }
}
