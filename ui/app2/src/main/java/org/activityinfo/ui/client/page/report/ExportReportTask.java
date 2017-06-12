package org.activityinfo.ui.client.page.report;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.RenderElement;
import org.activityinfo.legacy.shared.command.result.UrlResult;
import org.activityinfo.legacy.shared.reports.model.ReportElement;
import org.activityinfo.ui.client.dispatch.Dispatcher;

public class ExportReportTask implements ExportDialog.AsyncTask {
    private Dispatcher dispatcher;
    private RenderElement.Format format;
    private ReportElement model;
    private String filename;

    public ExportReportTask(Dispatcher dispatcher, RenderElement.Format format, ReportElement model, String filename) {
        this.dispatcher = dispatcher;
        this.format = format;
        this.model = model;
        this.filename = filename;
    }


    @Override
    public void start(final AsyncCallback<ExportDialog.AsyncTaskPoller> callback) {
        RenderElement command = new RenderElement(model, format);
        command.setFilename(filename);

        dispatcher.execute(command, new AsyncCallback<UrlResult>() {

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(final UrlResult urlResult) {
                callback.onSuccess(new ExportDialog.AsyncTaskPoller() {

                    @Override
                    public void poll(ExportDialog.ProgressCallback callback) {
                        callback.onDownloadReady(urlResult.getUrl());
                    }
                });
            }
        });
    }
}
