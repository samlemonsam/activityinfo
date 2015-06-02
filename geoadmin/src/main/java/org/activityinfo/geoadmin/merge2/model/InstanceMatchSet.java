package org.activityinfo.geoadmin.merge2.model;

import com.google.common.base.Optional;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.ObservableSet;

import java.util.*;

/**
 * Set of explicit {@link org.activityinfo.geoadmin.merge2.model.InstanceMatch}es
 * between <em>source</em> form instances and <em>target</em> instances.
 * 
 * <p>This {@link org.activityinfo.observable.ObservableSet} implementation ensures that 
 * all source instances are matched to at most one target instance, and that all
 * target instances are matched to at most one source instance.</p>
 */
public class InstanceMatchSet extends ObservableSet<InstanceMatch> {
    
    private Map<ResourceId, InstanceMatch> map = new HashMap<>();
    private Set<InstanceMatch> set = new HashSet<>();
    
    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public Set<InstanceMatch> asSet() {
        return Collections.unmodifiableSet(set);
    }

    /**
     * Adds the given {@code InstanceMatch} to the set, and removes any existing matches between
     * the given {@code match}'s source or target instance ids.
     * 
     * <p>Registered {@link org.activityinfo.observable.SetObserver}s are notified of the addition
     * as well as removes of any existing matches linked to the added match's source or target instances.</p>
     * 
     */
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

    /**
     * Removes the given match between target and source instances from the set, notifying
     * any registered 
     * @param match
     */
    public void remove(InstanceMatch match) {
        boolean removed = set.remove(match);
        map.remove(match.getTargetId());
        map.remove(match.getSourceId());
        if(removed) {
            fireRemoved(match);
        }
    }

    /**
     * Returns a match involving the given {@code resourceId} if one is present.
     */
    public Optional<InstanceMatch> find(ResourceId resourceId) {
        return Optional.fromNullable(map.get(resourceId));
    }
}
