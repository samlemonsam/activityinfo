package org.activityinfo.ui.client.page.report;

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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import org.activityinfo.i18n.shared.I18N;

public class ExportDialog extends Dialog {

    public interface AsyncTaskPoller {
        void poll(ProgressCallback callback);
    }

    public interface AsyncTask {
        void start(AsyncCallback<AsyncTaskPoller> callback);
    }

    public interface ProgressCallback {
        void onProgress(double percent);
        void onDownloadReady(String url);
        void onFailure(Throwable caught);
    }

    private ProgressBar bar;
    private Text downloadLink;
    private boolean canceled = false;
    private Button button;

    public ExportDialog() {

        setWidth(350);
        setHeight(175);
        setHeadingText(I18N.CONSTANTS.export());
        setClosable(false);
        setButtonAlign(HorizontalAlignment.CENTER);

        VBoxLayout layout = new VBoxLayout();
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);

        setLayout(layout);

        bar = new ProgressBar();
        bar.setWidth(300);
        add(bar, new VBoxLayoutData(new Margins(20, 15, 25, 15)));

        downloadLink = new Text(I18N.CONSTANTS.clickToDownload());
        downloadLink.setTagName("a");
        downloadLink.setVisible(false);
        add(downloadLink, new VBoxLayoutData(0, 15, 0, 15));

    }

    @Override
    protected void createButtons() {

        button = new Button();
        button.setText(I18N.CONSTANTS.cancel());
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                ExportDialog.this.canceled = true;
                bar.reset();
                hide();
            }
        });
        getButtonBar().add(button);
    }

    public void start(AsyncTask task) {
        showStartProgress();
        task.start(new AsyncCallback<AsyncTaskPoller>() {
            @Override
            public void onFailure(Throwable caught) {
                showError();
            }

            @Override
            public void onSuccess(AsyncTaskPoller poller) {
                if(!canceled) {
                    schedulePoll(poller, new AsyncCallback<String>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            showError();
                        }

                        @Override
                        public void onSuccess(String url) {
                            initiateDownload(url);
                        }
                    });
                }
            }
        });
    }


    private void showStartProgress() {
        show();
        bar.updateText(I18N.CONSTANTS.exportProgress());
        bar.auto();
    }

    private void showError() {
        MessageBox.alert(I18N.CONSTANTS.export(), I18N.CONSTANTS.serverError(), new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                ExportDialog.this.hide();
            }
        });
    }

    private void initiateDownload(String url) {
        bar.reset();
        bar.updateProgress(1.0, I18N.CONSTANTS.downloadReady());
        button.setText(I18N.CONSTANTS.close());
        tryStartDownloadWithIframe(url);
        downloadLink.getElement().setAttribute("href", url);
        downloadLink.setVisible(true);
        layout(true);
    }

    private void schedulePoll(final AsyncTaskPoller poller, final AsyncCallback<String> urlCallback) {
        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                if(!canceled) {
                    pollServer(poller, urlCallback);
                }
                return false;
            }
        }, 1000);
    }

    private void pollServer(final AsyncTaskPoller poller, final AsyncCallback<String> urlCallback) {
        poller.poll(new ProgressCallback() {
            @Override
            public void onProgress(double percent) {
                bar.updateProgress(percent, I18N.CONSTANTS.exportProgress());
                schedulePoll(poller, urlCallback);
            }

            @Override
            public void onDownloadReady(String url) {
                urlCallback.onSuccess(url);
            }

            @Override
            public void onFailure(Throwable caught) {
                urlCallback.onFailure(caught);
            }
        });
    }

    private void tryStartDownloadWithIframe(String url) {
        com.google.gwt.user.client.ui.Frame frame = new com.google.gwt.user.client.ui.Frame(url);
        El el = El.fly(frame.getElement());
        el.setStyleAttribute("width", 0);
        el.setStyleAttribute("height", 0);
        el.setStyleAttribute("position", "absolute");
        el.setStyleAttribute("border", 0);
        RootPanel.get().add(frame);
    }
}
