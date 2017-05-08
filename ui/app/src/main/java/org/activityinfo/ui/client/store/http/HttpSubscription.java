package org.activityinfo.ui.client.store.http;

/**
 * A reference to a {@link HttpRequest} submitted to the {@link HttpBus} that can be used
 * to cancel the request.
 */
public interface HttpSubscription {

    void cancel();

}
