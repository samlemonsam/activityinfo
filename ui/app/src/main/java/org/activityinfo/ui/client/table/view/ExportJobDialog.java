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
package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.Padding;
import com.sencha.gxt.widget.core.client.AutoProgressBar;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer;
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
        downloadLink.setWidth("200px");

        VBoxLayoutContainer container = new VBoxLayoutContainer(VBoxLayoutContainer.VBoxLayoutAlign.CENTER);
        container.setPadding(new Padding(10));
        container.add(progressBar);
        container.add(downloadLink, new BoxLayoutContainer.BoxLayoutData(new Margins(20, 0, 0, 0)));

        dialog = new Dialog();
        dialog.setHeading(I18N.CONSTANTS.download());
        dialog.setPredefinedButtons(Dialog.PredefinedButton.CANCEL);
        dialog.addDialogHideHandler(this::onDialogHide);
        dialog.setWidget(container);
        dialog.setPixelSize(450, 200);
        dialog.setHideOnButtonClick(true);
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
                    dialog.forceLayout();

                    ExportJobDialog.this.dialog.getButton(Dialog.PredefinedButton.CANCEL).setText(I18N.CONSTANTS.close());
                } else if (observable.get().getState() == JobState.FAILED) {
                    progressBar.reset();
                    progressBar.updateProgress(1, I18N.CONSTANTS.error());
                    ExportJobDialog.this.dialog.getButton(Dialog.PredefinedButton.CANCEL).setText(I18N.CONSTANTS.close());

                    AlertMessageBox warning = new AlertMessageBox(I18N.CONSTANTS.error(),I18N.CONSTANTS.errorOnServer());
                    warning.show();
                }
            }
        });
    }

    private void onDialogHide(DialogHideEvent dialogHideEvent) {
        this.jobSubscription.unsubscribe();
    }

}
