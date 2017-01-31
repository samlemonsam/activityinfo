package org.activityinfo.ui.client.widget.loading;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.ui.client.widget.Button;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Displays the loading state of a table
 */
public class TableLoadingIndicator implements IsWidget, LoadingView {

    private static final Logger LOGGER = Logger.getLogger(TableLoadingIndicator.class.getName());
    
    private final HTMLPanel rootElement;

    @UiField
    Button retryButton;
    @UiField
    SpanElement messageSpan;

    private boolean hideOnSuccess;
    private LoadingState state = LoadingState.NONE;

    interface LoadingIndicatorUiBinder extends UiBinder<HTMLPanel, TableLoadingIndicator> {
    }

    private static LoadingIndicatorUiBinder ourUiBinder = GWT.create(LoadingIndicatorUiBinder.class);

    public TableLoadingIndicator() {
        rootElement = ourUiBinder.createAndBindUi(this);
    }

    @Override
    public void onLoadingStateChanged(final LoadingState state, final Throwable caught) {
        LOGGER.log(Level.SEVERE, "Table Load failed", caught);
        onLoadingStateChanged(state, ExceptionOracle.getHeading(caught));
    }

    public TableLoadingIndicator onLoadingStateChanged(final LoadingState state) {
        return onLoadingStateChanged(state, "");
    }

    public TableLoadingIndicator onLoadingStateChanged(final LoadingState state, final String message) {
        this.state = state;

        rootElement.setVisible(visible(state));

        messageSpan.removeClassName("text-warning");
        if (state == LoadingState.FAILED) {
            messageSpan.addClassName("text-warning");
        }

        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                ExceptionOracle.setLoadingStyle(rootElement, state);
                messageSpan.setInnerText(message);
                return false;
            }
        }, 500);
        return this;
    }

    private boolean visible(LoadingState state) {
        if (hideOnSuccess && state == LoadingState.LOADED) {
            return false;
        }
        return true;
    }

    @Override
    public Button getRetryButton() {
        return retryButton;
    }

    @Override
    public Widget asWidget() {
        return rootElement;
    }

    public boolean isHideOnSuccess() {
        return hideOnSuccess;
    }

    public TableLoadingIndicator setHideOnSuccess(boolean hideOnSuccess) {
        this.hideOnSuccess = hideOnSuccess;
        return this;
    }

    public LoadingState getState() {
        return state;
    }

    public boolean isLoading() {
        return state == LoadingState.LOADING;
    }
}