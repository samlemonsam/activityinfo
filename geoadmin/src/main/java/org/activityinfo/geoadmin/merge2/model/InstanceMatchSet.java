package org.activityinfo.geoadmin.merge2.model;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.ObservableSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class InstanceMatchSet extends ObservableSet<InstanceMatch> {
    
    private Map<ResourceId, InstanceMatch> map = new HashMap<>();
    private Set<InstanceMatch> set = new HashSet<>();
    
    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public Set<InstanceMatch> get() {
        return set;
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
        if(removed) {
            fireRemoved(match);
        }
    }
}
