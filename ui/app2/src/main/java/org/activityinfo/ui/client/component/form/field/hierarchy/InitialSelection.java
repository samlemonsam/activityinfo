package org.activityinfo.ui.client.component.form.field.hierarchy;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.dispatch.ResourceLocator;

import javax.annotation.Nullable;
import java.util.ArrayList;
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

    private Promise<Void> fetchLabelAndParentIds(final ResourceLocator locator, final Set<RecordRef> references) {


        List<Promise<Void>> promises = new ArrayList<>();
        for (RecordRef reference : references) {
            promises.add(locator.getFormInstance(reference.getFormId(), reference.getRecordId())
                    .join(new Function<FormInstance, Promise<Void>>() {

                        @Nullable
                        @Override
                        public Promise<Void> apply(@Nullable FormInstance instance) {
                            Set<RecordRef> parentsToFetch = populateSelection(instance);
                            if (parentsToFetch.isEmpty()) {
                                return Promise.done();
                            } else {
                                return fetchLabelAndParentIds(locator, parentsToFetch);
                            }
                        }
                    }));
        }

        return Promise.waitAll(promises);
    }

    private Set<RecordRef> populateSelection(FormInstance instance) {
        Set<RecordRef> parents = Sets.newHashSet();

        Level level = hierarchy.getLevel(instance.getFormId());
        if(level != null) {
            Choice choice = level.toChoice(instance);
            selection.put(choice.getRef().getFormId(), choice);
            if(!level.isRoot()) {
                parents.add(choice.getParentRef());
            }
        }
        return parents;
    }

    public Map<ResourceId, Choice> getSelection() {
        return selection;
    }
}
