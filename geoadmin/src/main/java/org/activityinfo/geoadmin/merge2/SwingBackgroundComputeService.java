package org.activityinfo.geoadmin.merge2;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.activityinfo.observable.ComputeService;
import org.activityinfo.observable.Observable;

import javax.swing.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;


public class SwingBackgroundComputeService implements ComputeService {


    private final ListeningExecutorService executorService;

    public SwingBackgroundComputeService(ListeningExecutorService executorService) {
        this.executorService = executorService;
    }


    @Override
    public <T, U> Observable<U> transform(Observable<T> argument, Function<T, U> function) {
        throw new UnsupportedOperationException();
    }


    private abstract class BackgroundComputedValue<T, R> extends Observable<R> {

        private Observable<T> argument;
        private ListenableFuture<R> value;

        public BackgroundComputedValue(Observable<T> argument, ListenableFuture<R> value) {
            this.argument = argument;
            this.value = value;
        }

        @Override
        protected void onConnect() {

            this.value = executorService.submit(new Callable<R>() {
                @Override
                public R call() throws Exception {
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

        protected abstract R execute();

        @Override
        protected void onDisconnect() {
            super.onDisconnect();
        }

        @Override
        public boolean isLoading() {
            return value == null || !value.isDone();
        }

        @Override
        public R get() {
            return Futures.getUnchecked(value);
        }
    }

}
