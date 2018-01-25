package org.activityinfo.observable;

import java.util.ArrayList;
import java.util.List;

public class RemoteServiceStub {

    private List<RemoteCall> pendingCalls = new ArrayList<>();
    
    public Observable<String> queryName(int id) {
        RemoteCall call = new RemoteCall(id);
        pendingCalls.add(call);
        return call.value;
    }

    public void completePending() {
        for (RemoteCall pendingCall : pendingCalls) {
            pendingCall.complete();
        }
        pendingCalls.clear();
    }


    private static class RemoteCall {
        private int id;
        private ObservableStub<String> value = new ObservableStub<>();

        public RemoteCall(int id) {
            this.id = id;
        }

        public void complete() {
            value.updateValue("name" + id);
        }
    }
    
    
    
}
