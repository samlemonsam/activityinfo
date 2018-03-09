/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.widget;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.widget.loading.LoadingPanelView;
import org.activityinfo.ui.client.widget.loading.LoadingState;
import org.activityinfo.ui.client.widget.loading.PageLoadingPanel;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SimpleLayoutPanel for widgets that need to be asynchronously
 * created
 */
public class LoadingPanel<V> implements IsWidget {

    private static final Logger LOGGER = Logger.getLogger(LoadingPanel.class.getName());

    /**
     * We get confused when things fail to quickly. I mean really, it's like you're not
     * even trying...
     */
    public static final int DELAY_MS = 1000;


    /**
     * A function which provides the widget based on the resolved value
     */
    private Function<? super V, ? extends DisplayWidget<? super V>> widgetProvider;

    /**
     * Provides the value to be displayed by the display widget. We use a provider so that
     * can retry if it fails at first.
     */
    private Provider<Promise<V>> valueProvider;

    private final LoadingPanelView loadingView;

    private int currentRequestNumber = 0;

    private HandlerRegistration retryHandler;

    public LoadingPanel() {
        this(new PageLoadingPanel());
    }

    public LoadingPanel(PageLoadingPanel view) {
        this.loadingView = view;
    }

    public void setDisplayWidget(DisplayWidget<? super V> widget) {
        this.widgetProvider = Functions.constant(widget);
    }

    public void setDisplayWidgetProvider(Function<V, ? extends DisplayWidget<? super V>> function) {
        this.widgetProvider = function;
    }

    public Promise<Void> show(Provider<Promise<V>> provider) {
        this.valueProvider = provider;
        return tryLoad();
    }

    public <T> Promise<Void> show(final Function<T, Promise<V>> function, final T argument) {
        return show(new Provider<Promise<V>>() {

            @Override
            public Promise<V> get() {
                return function.apply(argument);
            }
        });
    }

    public Promise<Void> showWithoutLoad() {
        return show(new Provider<Promise<V>>() {
            @Override
            public Promise<V> get() {
                return Promise.resolved(null);
            }
        });
    }

    private Promise<Void> tryLoad() {
        Promise<V> promisedValue = valueProvider.get();

        // make sure we only react to the last request submitted...
        final int requestNumber = currentRequestNumber+1;
        this.currentRequestNumber = requestNumber;

        loadingView.onLoadingStateChanged(LoadingState.LOADING, null);

        Promise<Void> loadResult = promisedValue.then(new Function<V, Void>() {
            @Override
            public Void apply(@Nullable V result) {
                if (requestNumber == currentRequestNumber) {
                    try {
                        showWidget(requestNumber, result);
                    } catch (Throwable e) {
                        showLoadFailure(requestNumber, e);
                    }
                }
                return null;
            }
        });

        // handle failure (including the failure of the show() method of our
        // display widget)

        loadResult.then(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                if (requestNumber == currentRequestNumber) {
                    showLoadFailure(requestNumber, caught);
                }
            }

            @Override
            public void onSuccess(Void result) {
                // already handled above
            }
        });

        return loadResult;
    }

    private void showLoadFailure(final int requestNumber, final Throwable caught) {

        LOGGER.log(Level.SEVERE, "Load failed", caught);

        // the failure may have been caught upstream
        if(!loadingView.asWidget().isAttached()) {
            return;
        }

        if(retryHandler == null) {
            retryHandler = loadingView.getRetryButton().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    tryLoad();
                }
            });
        }

        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                loadingView.onLoadingStateChanged(LoadingState.FAILED, caught);
                return false;
            }
        }, DELAY_MS);
    }

    private void setWidgetWithDelay(final int requestNumber, final IsWidget widget) {
        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                if(requestNumber == currentRequestNumber) {
                    loadingView.setWidget(widget);
                }
                return false;
            }
        }, DELAY_MS);
    }

    private void showWidget(final int requestNumber, V result) {
        assert widgetProvider != null : "No widget/provider has been set!";
        try {
            final DisplayWidget<? super V> displayWidget = widgetProvider.apply(result);
            displayWidget.show(result).then(new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    showLoadFailure(requestNumber, caught);
                }

                @Override
                public void onSuccess(Void result) {
                    setWidgetWithDelay(requestNumber, displayWidget);
                }
            });
        } catch(Throwable caught) {
            showLoadFailure(requestNumber, caught);
        }
    }

    @Override
    public Widget asWidget() {
        return loadingView.asWidget();
    }
}
