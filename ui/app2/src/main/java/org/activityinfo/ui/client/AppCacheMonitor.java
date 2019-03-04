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
package org.activityinfo.ui.client;

import com.bedatadriven.rebar.appcache.client.AppCache;
import com.bedatadriven.rebar.appcache.client.AppCacheFactory;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Singleton;
import org.activityinfo.i18n.shared.I18N;

@Singleton
public class AppCacheMonitor {

    /**
     * This client version is incompatible with the server version and *must* be reloaded.
     */
    private boolean incompatibleRemoteService = false;

    /**
     * A new update has been downloaded and is ready to serve
     */
    private boolean updateReady = false;

    /**
     * We have started reloading the window
     */
    private boolean reloading = false;
    private AppCache appCache;

    public void start() {
        appCache = AppCacheFactory.get();
        appCache.addUpdateReadyHandler(() -> {
            updateReady = true;
            if (incompatibleRemoteService) {
                notifyForceReload();
            } else {
                promptReload();
            }
        });
    }

    /**
     * Reports an IncompatibleRemoteServiceException encountered by the Dispatcher.
     */
    public void onSerializationException() {
        if (incompatibleRemoteService) {
            return;
        } else {
            incompatibleRemoteService = true;
        }
        if (updateReady) {
            notifyForceReload();
        } else {
            // The appcache is most likely updating in the background, give some time to
            // download
            appCache.ensureUpToDate(new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    removeCacheAndForceReload();
                }

                @Override
                public void onSuccess(Void result) {
                    notifyForceReload();
                }
            });

            // Add a cutoff for downloading the new cache - at some point give up and
            // force a reload
            Scheduler.get().scheduleFixedDelay(this::removeCacheAndForceReload, 5_000);
        }
    }

    private boolean removeCacheAndForceReload() {
        if (updateReady) {
            notifyForceReload();
        }
        appCache.removeCache(new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                notifyForceReload();
            }

            @Override
            public void onSuccess(Void result) {
                notifyForceReload();
            }
        });
        return false;
    }

    private void promptReload() {
        MessageBox.confirm(I18N.MESSAGES.newVersion(ClientContext.getAppTitle()),
                I18N.CONSTANTS.newVersionChoice(),
                be -> {
                    if (be.getButtonClicked().getItemId()
                            .equals(Dialog.YES)) {
                        doReload();
                    }
                });
    }

    private void notifyForceReload() {
        if (reloading) {
            return;
        }
        Window.alert(I18N.CONSTANTS.newVersionChoiceForce());
        doReload();
    }

    private void doReload() {
        reloading = true;
        Window.Location.reload();
    }


}
