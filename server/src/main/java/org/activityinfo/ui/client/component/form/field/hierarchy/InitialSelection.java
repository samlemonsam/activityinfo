package org.activityinfo.ui.client.component.form.field.hierarchy;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Promise;

import java.util.List;
import java.util.Map;
import java.util.Set;

class InitialSelection {

    private final Hierarchy hierarchy;
    private final Map<ResourceId, Choice> selection = Maps.newHashMap();

    public InitialSelection(Hierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    public Promise<Void> fetch(ResourceLocator locator, Set<RecordRef> recordIds) {
        if(recordIds == null || recordIds.isEmpty()) {
            return Promise.done();
        } else {
            return fetchLabelAndParentIds(locator, recordIds);
        }
    }

    private Promise<Void> fetchLabelAndParentIds(final ResourceLocator locator, Set<RecordRef> recordIds) {
        return Promise.rejected(new UnsupportedOperationException("TODO"));
        
//        QueryModel queryModel = new QueryModel();
//        for (Level level : hierarchy.getLevels()) {
//            queryModel.addRowSource(level.getClassId());
//        }
//        queryModel.selectResourceId().as("id");
//        queryModel.selectExpr("label").as("label");
//        queryModel.selectExpr("parent").as("parent");
//        queryModel.setFilter(Exprs.idEqualTo(recordIds));
//        
//        queryModel.setFilter();
//        
//        InstanceQuery query = InstanceQuery
//                .select(LABEL_PROPERTY, PARENT_PROPERTY)
//                .where(new IdCriteria(null, recordIds))
//                .build();
//
//        return locator.query(query)
//                  .join(new Function<List<Projection>, Promise<Void>>() {
//                      @Override
//                      public Promise<Void> apply(List<Projection> projections) {
//
//                          Set<ResourceId> parents = populateSelection(projections);
//                          if (parents.isEmpty()) {
//                              return Promise.done();
//                          } else {
//                              return fetchLabelAndParentIds(locator, parents);
//                          }
//                      }
//                  });
    }

    private Set<ResourceId> populateSelection(List<Choice> choices) {
        Set<ResourceId> parents = Sets.newHashSet();
        for(Choice choice : choices) {
            Level level = hierarchy.getLevel(choice.getRootClassId());
            if(level != null) {
                selection.put(choice.getRootClassId(), choice);
                if(!level.isRoot()) {
                    ResourceId parentId = choice.getParentId();
                    assert parentId != null;
                    parents.add(parentId);
                }
            }
        }
        parents.removeAll(selection.keySet());
        return parents;
    }

    public Map<ResourceId, Choice> getSelection() {
        return selection;
    }
}
