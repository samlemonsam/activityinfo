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
package org.activityinfo.ui.client.component.formdesigner.container;

import com.google.gwt.user.client.ui.FocusPanel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.dispatch.callback.SuccessCallback;
import org.activityinfo.ui.client.style.ElementStyle;
import org.activityinfo.ui.client.widget.ConfirmDialog;

/**
 * @author yuriyz on 11/25/2014.
 */
public class DeleteWidgetContainerAction implements ConfirmDialog.Action {

//    private static final Logger LOGGER = Logger.getLogger(DeleteFormFieldAction.class.getName());

    private final FocusPanel focusPanel;
    private final FormDesigner formDesigner;
    private SuccessCallback<Object> successCallback = null;

    public DeleteWidgetContainerAction(FocusPanel focusPanel, FormDesigner formDesigner) {
        this.focusPanel = focusPanel;
        this.formDesigner = formDesigner;
    }

    @Override
    public ConfirmDialog.Messages getConfirmationMessages() {
        return new ConfirmDialog.Messages(
                I18N.CONSTANTS.confirmDeletion(),
                I18N.CONSTANTS.deleteFormFieldConfirmation(),
                I18N.CONSTANTS.delete());
    }

    @Override
    public ConfirmDialog.Messages getProgressMessages() {
        return new ConfirmDialog.Messages(
                I18N.CONSTANTS.deletionInProgress(),
                I18N.CONSTANTS.deleting(),
                I18N.CONSTANTS.deleting());
    }

    @Override
    public ConfirmDialog.Messages getFailureMessages() {
        return new ConfirmDialog.Messages(
                I18N.CONSTANTS.deletionFailed(),
                I18N.CONSTANTS.retryDeletion(),
                I18N.CONSTANTS.retry());
    }

    @Override
    public ElementStyle getPrimaryButtonStyle() {
        return ElementStyle.DANGER;
    }

    @Override
    public Promise<Void> execute() {
        focusPanel.removeFromParent();
        Promise<Void> promise = Promise.resolved(null);
        if (successCallback != null) {
            promise.then(successCallback);
        }
        return promise;
    }

    @Override
    public void onComplete() {
        formDesigner.getSavedGuard().setSaved(false);
    }
}