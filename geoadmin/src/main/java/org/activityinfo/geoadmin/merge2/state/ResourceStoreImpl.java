package org.activityinfo.geoadmin.merge2.state;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.activityinfo.geoadmin.model.ActivityInfoClient;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;

import javax.swing.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class ResourceStoreImpl implements ResourceStore {
    
    private final ActivityInfoClient client;
    private final ListeningExecutorService executorService;

    public ResourceStoreImpl(ActivityInfoClient client) {
        this.client = client;
        executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    }

    @Override
    public Observable<FormTree> getFormTree(final ResourceId resourceId) {
        return new ObservableQuery<FormTree>() {
            @Override
            protected FormTree execute() {
                return client.getFormTree(resourceId);
            }
        };
    }

    @Override
    public Observable<ColumnSet> queryColumns(final QueryModel queryModel) {
        return new ObservableQuery<ColumnSet>() {

            @Override
            protected ColumnSet execute() {
                return client.queryColumns(queryModel);
            }
        };
    }

    private abstract class ObservableQuery<T> extends Observable<T> {

        private ListenableFuture<T> value;

        @Override
        protected void onConnect() {
            this.value = executorService.submit(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    return execute();
                }
            });
            this.value.addListener(new Runnable() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            fireChange();
                        }
                    });
                }
            }, executorService);
        }

        protected abstract T execute();

        @Override
        protected void onDisconnect() {
            super.onDisconnect();
        }

        @Override
        public boolean isLoading() {
            return value == null || !value.isDone();
        }

        @Override
        public T get() {
            return Futures.getUnchecked(value);
        }
    }
}
