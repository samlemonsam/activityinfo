package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Function;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Scheduler;


public class InstanceMatchGraph extends MatchGraph {
    
    private KeyFieldPairSet keyFields;

    public InstanceMatchGraph(KeyFieldPairSet keyFields) {
        super(new InstanceMatrix(keyFields));
        this.keyFields = keyFields;
    }

    public KeyFieldPairSet getKeyFields() {
        return keyFields;
    }

    public static Observable<InstanceMatchGraph> build(Scheduler scheduler, Observable<KeyFieldPairSet> keyFields) {
        return keyFields.transform(scheduler, new Function<KeyFieldPairSet, InstanceMatchGraph>() {
            @Override
            public InstanceMatchGraph apply(KeyFieldPairSet input) {
                InstanceMatchGraph graph = new InstanceMatchGraph(input);
                graph.build();
                return graph;
            }
        });
    }
}
