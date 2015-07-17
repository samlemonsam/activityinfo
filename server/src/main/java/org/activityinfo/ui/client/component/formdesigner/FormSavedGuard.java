package org.activityinfo.ui.client.component.formdesigner;
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

import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.legacy.client.AsyncMonitor;
import org.activityinfo.legacy.client.callback.SuccessCallback;
import org.activityinfo.ui.client.page.HasNavigationCallback;
import org.activityinfo.ui.client.page.NavigationCallback;
import org.activityinfo.ui.client.page.common.dialog.SaveChangesCallback;
import org.activityinfo.ui.client.page.common.dialog.SavePromptMessageBox;

/**
 * @author yuriyz on 11/24/2014.
 */
public class FormSavedGuard implements HasNavigationCallback {

    public interface HasSavedGuard {
        HasNavigationCallback getSavedGuard();
    }

    private final FormDesigner formDesigner;

    private boolean saved = true;

    public FormSavedGuard(FormDesigner formDesigner) {
        this.formDesigner = formDesigner;
    }

    @Override
    public void navigate(final NavigationCallback callback) {
        if (!saved) {
            final SavePromptMessageBox box = new SavePromptMessageBox();
            box.show(new SaveChangesCallback() {

                @Override
                public void save(final AsyncMonitor monitor) {
                    formDesigner.getFormDesignerActions().save().then(new SuccessCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            box.hide();
                            callback.onDecided(true);
                        }
                    });
                }

                @Override
                public void discard() {
                    box.hide();
                    callback.onDecided(true);
                }

                @Override
                public void cancel() {
                    box.hide();
                    callback.onDecided(false);
                }
            });
        } else {
            callback.onDecided(true);
        }
    }

    public boolean isSaved() {
        return this.saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public static FormSavedGuard getGuard(Widget widget) {
        if (widget instanceof FormSavedGuard.HasSavedGuard) {
            return (FormSavedGuard) ((HasSavedGuard) widget).getSavedGuard();
        }
        if (widget instanceof HasOneWidget) {
            return getGuard(((HasOneWidget) widget).getWidget());
        } else if (widget instanceof IndexedPanel) {
            IndexedPanel indexedPanel = (IndexedPanel) widget;
            for (int i = 0; i < indexedPanel.getWidgetCount(); i++) {
                Widget w = indexedPanel.getWidget(i);
                FormSavedGuard guard = getGuard(w);
                if (guard != null) {
                    return guard;
                }
            }
        }
        return null;
    }


    /**
     * @return true HasNavigationCallback was found, otherwise false
     */
    public static boolean callNavigationCallback(Widget widget, NavigationCallback callback) {
        if (widget instanceof HasNavigationCallback) {
            ((HasNavigationCallback) widget).navigate(callback);
            return true;
        }
        if (widget instanceof HasOneWidget) {
            if (callNavigationCallback(((HasOneWidget) widget).getWidget(), callback)) {
                return true;
            }
        } else if (widget instanceof IndexedPanel) {
            IndexedPanel indexedPanel = (IndexedPanel) widget;
            for (int i = 0; i < indexedPanel.getWidgetCount(); i++) {
                Widget w = indexedPanel.getWidget(i);
                if (callNavigationCallback(w, callback)) {
                    return true;
                }
            }
        }
        return false;
    }
}
