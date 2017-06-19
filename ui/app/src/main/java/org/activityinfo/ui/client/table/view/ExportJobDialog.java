package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.util.Padding;
import com.sencha.gxt.widget.core.client.AutoProgressBar;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.job.ExportFormJob;
import org.activityinfo.model.job.ExportResult;
import org.activityinfo.model.job.JobState;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;

public class ExportJobDialog {

    private Dialog dialog;
    private Observable<JobStatus<ExportFormJob, ExportResult>> jobStatus;
    private Subscription jobSubscription;
    private final AutoProgressBar progressBar;
    private final Anchor downloadLink;

    public ExportJobDialog(Observable<JobStatus<ExportFormJob, ExportResult>> jobStatus) {
        this.jobStatus = jobStatus;

        progressBar = new AutoProgressBar();
        progressBar.setInterval(1000);

        downloadLink = new Anchor(I18N.CONSTANTS.clickToDownload());
        downloadLink.setVisible(false);
        downloadLink.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        VBoxLayoutContainer container = new VBoxLayoutContainer(VBoxLayoutContainer.VBoxLayoutAlign.CENTER);
        container.setPadding(new Padding(10));
        container.add(progressBar);
        container.add(downloadLink);

        dialog = new Dialog();
        dialog.setHeading(I18N.CONSTANTS.download());
        dialog.setPredefinedButtons(Dialog.PredefinedButton.CANCEL);
        dialog.addDialogHideHandler(this::onDialogHide);
        dialog.setWidget(container);
        dialog.setPixelSize(450, 300);
    }

    public void show() {
        this.dialog.show();
        this.progressBar.auto();
        this.jobSubscription = jobStatus.subscribe(observable -> {
            if(observable.isLoaded()) {
                if (observable.get().getState() == JobState.COMPLETED) {
                    progressBar.reset();
                    progressBar.updateProgress(1, I18N.CONSTANTS.downloadReady());
                    downloadLink.setVisible(true);
                    downloadLink.setHref(observable.get().getResult().getDownloadUrl());
                    ExportJobDialog.this.dialog.getButton(Dialog.PredefinedButton.CANCEL).setText(I18N.CONSTANTS.close());
                }
            }
        });
    }

    private void onDialogHide(DialogHideEvent dialogHideEvent) {
        this.jobSubscription.unsubscribe();
    }

}
