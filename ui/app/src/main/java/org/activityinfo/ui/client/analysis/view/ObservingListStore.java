package org.activityinfo.ui.client.analysis.view;

import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import org.activityinfo.observable.ListObserver;
import org.activityinfo.observable.ObservableList;
import org.activityinfo.observable.Subscription;

/**
 *
 */
public class ObservingListStore<T> extends ListStore<T> {

    private final ObservableList<T> source;

    private final Subscription subscription;

    /**
     * Creates a new store.
     *
     * @param keyProvider the key provider
     * @param source
     */
    public ObservingListStore(ObservableList<T> source, ModelKeyProvider<? super T> keyProvider) {
        super(keyProvider);
        this.source = source;
        this.subscription = source.subscribe(new ListObserver<T>() {
            @Override
            public void onChange() {
                if (source.isLoading()) {
                    ObservingListStore.this.clear();
                } else {
                    ObservingListStore.this.replaceAll(source.asList());
                }
            }

            @Override
            public void onElementAdded(T element) {
                ObservingListStore.this.add(element);
            }

            @Override
            public void onElementRemoved(T element) {
                ObservingListStore.this.remove(element);
            }
        });
    }

    public void disconnect() {
        subscription.unsubscribe();
    }

}
