package org.activityinfo.geoadmin.merge2.model;

import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.ObservableSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Set of user-defined matchings between <em>source</em> form instances
 * and <em>target</em> instances.
 */
public class InstanceMatchSet extends ObservableSet<InstanceMatch> {
    
    private Map<ResourceId, InstanceMatch> map = new HashMap<>();
    private Set<InstanceMatch> set = new HashSet<>();
    
    @Override
    public boolean isLoading() {
        return false;
    }

    
    public void add(InstanceMatch match) {
        if(!set.contains(match)) {
            removeMatchesWith(match.getSourceId());
            removeMatchesWith(match.getTargetId());

            map.put(match.getSourceId(), match);
            map.put(match.getTargetId(), match);
            set.add(match);
            fireAdded(match);
        }
    }

    private void removeMatchesWith(ResourceId id) {
        InstanceMatch match = map.remove(id);
        if(match != null) {
            set.remove(match);
            fireRemoved(match);
        }
    }

    public void remove(InstanceMatch match) {
        boolean removed = set.remove(match);
        map.remove(match.getTargetId());
        map.remove(match.getSourceId());
        if(removed) {
            fireRemoved(match);
        }
    }

    public Optional<InstanceMatch> find(ResourceId resourceId) {
        return Optional.fromNullable(map.get(resourceId));
    }
}
