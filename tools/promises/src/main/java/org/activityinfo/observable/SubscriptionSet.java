package org.activityinfo.observable;

import java.util.ArrayList;
import java.util.List;


public class SubscriptionSet {
    
    private List<Subscription> subscriptions = new ArrayList<>();
    
    public SubscriptionSet() {
    }
    
    public void add(Subscription subscription) {
        subscriptions.add(subscription);
    }
    
    public void unsubscribeAll() {
        for (Subscription subscription : subscriptions) {
            subscription.unsubscribe();
        }
        subscriptions.clear();
    }
}
