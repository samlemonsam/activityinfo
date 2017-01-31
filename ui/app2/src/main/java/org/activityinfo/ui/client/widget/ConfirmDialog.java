package org.activityinfo.ui.client.widget;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.style.ElementStyle;
import org.activityinfo.ui.client.widget.loading.ExceptionOracle;

/**
 * @author yuriyz on 4/1/14.
 */
public class ConfirmDialog  {


    /**
     * Maintain a single instance of this dialog, as there is by definition never more than one
     * modal dialog shown at a time.
     */
    private static ConfirmDialog INSTANCE = null;

    /**
     * @author yuriyz on 4/8/14.
     */
    public static class Messages {
        private String titleText;
        private String messageText;
        private String primaryButtonText;

        public Messages(String titleText, String messageText, String primaryButtonText) {
            this.titleText = titleText;
            this.messageText = messageText;
            this.primaryButtonText = primaryButtonText;
        }

        public String getTitleText() {
            return titleText;
        }

        public String getMessageText() {
            return messageText;
        }

        public String getPrimaryButtonText() {
            return primaryButtonText;
        }
    }

    /**
     * @author yuriyz on 4/7/14.
     */
    public static interface Action {

        Messages getConfirmationMessages();

        Messages getProgressMessages();

        Messages getFailureMessages();

        ElementStyle getPrimaryButtonStyle();

        /**
         *
         * Invoked when the user has confirmed the action, or is retrying.
         */
        Promise<Void> execute();

        /**
         * Invoked when the action completes successfully
         */
        void onComplete();
    }


    public static enum State {
        CONFIRM, PROGRESS, FAILED
    }

    private final ModalDialog dialog;
    private final HTML failedMessageContainer = new HTML();
    private final HTML messageContainer = new HTML();

    private State state;

    private Action action;

    private ConfirmDialog() {
        dialog = new ModalDialog();
        dialog.getModalBody().add(failedMessageContainer);
        dialog.getModalBody().add(messageContainer);

        dialog.getPrimaryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                tryAction();
            }
        });
    }

    /**
     * Shows the confirmation dialog
     */
    public static void confirm(Action action) {
        if(INSTANCE == null) {
            INSTANCE = new ConfirmDialog();
        }
        INSTANCE.action = action;
        INSTANCE.dialog.getPrimaryButton().setStyleName("btn btn-" + action.getPrimaryButtonStyle().name().toLowerCase());
        INSTANCE.updateState(State.CONFIRM, null);
        INSTANCE.dialog.show();
    }


    private void tryAction() {
        updateState(State.PROGRESS, null);
        action.execute().then(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                showFailureDelayed(caught);
            }

            @Override
            public void onSuccess(Void result) {
                ConfirmDialog.this.dialog.hide();
                action.onComplete();
            }
        });
    }

    private void showFailureDelayed(final Throwable caught) {
        // Show failure message only after a short fixed delay to ensure that
        // the progress stage is displayed. Otherwise if we have a synchronous error, clicking
        // the retry button will look like it's not working.
        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                updateState(State.FAILED, caught);
                return false;
            }
        }, 500);
    }

    private void updateState(State state, Throwable caught) {
        this.state = state;

        // Disable both ok and cancel button while we're waiting for the
        // asynchronous action to complete. We have no reliable way of cancelling
        // a delete action that has been sent to the server for example.

        dialog.getPrimaryButton().setEnabled(state != State.PROGRESS);
        dialog.getCancelButton().setEnabled(state != State.PROGRESS);

        failedMessageContainer.setVisible(state == State.FAILED);

        switch (state) {
            case CONFIRM:
                updateMessages(action.getConfirmationMessages());
                break;

            case FAILED:
                updateMessages(action.getFailureMessages());
                failedMessageContainer.setHTML(Templates.WARNING_MESSAGE_TEMPLATE.html(ExceptionOracle.getExplanation(caught)));
                break;

            case PROGRESS:
                updateMessages(action.getProgressMessages());
                break;
        }
    }

    private void updateMessages(Messages messages) {
        dialog.setDialogTitle(messages.getTitleText());
        messageContainer.setHTML(messages.getMessageText());

        if(state == State.PROGRESS) {
            dialog.getPrimaryButton().setHTML(Templates.OK_BTN_TEMPLATE.html(messages.getPrimaryButtonText()));
        } else {
            dialog.getPrimaryButton().setText(messages.getPrimaryButtonText());
        }
    }
}
