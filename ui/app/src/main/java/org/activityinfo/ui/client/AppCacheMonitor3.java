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

import com.google.gwt.core.client.Scheduler;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import org.activityinfo.i18n.shared.I18N;

import java.util.logging.Logger;

/**
 * Monitors for changes and the prompts the user to update when a new version
 * is available
 */
public class AppCacheMonitor3 {

    private static final Logger LOGGER = Logger.getLogger(AppCacheMonitor3.class.getName());


    private static final int EVERY_FIVE_MINUTES = 5 * 60 * 1000;

    private AppCache appCache;

    /**
     * True if the user has already been prompted during this session to
     * reload for an update.
     */
    private boolean userPrompted;

    public AppCacheMonitor3(AppCache appCache) {
        this.appCache = appCache;
    }

    public void start() {


        // Schedule regular checks for an update to the AppCache
        Scheduler.get().scheduleFixedDelay(appCache::checkForUpdates, EVERY_FIVE_MINUTES);

        // Listen for any changes to the status
        appCache.getStatus().subscribe(status -> {

            LOGGER.info("AppCache Status: " + status.get());

            if(status.get() == AppCache.Status.UPDATE_READY) {
                if(!userPrompted) {
                    userPrompted = true;
                    promptUser();
                }
            }
        });
    }

    private void promptUser() {
        MessageBox messageBox = new MessageBox("ActivityInfo");
        messageBox.setMessage(I18N.CONSTANTS.newVersionChoice());
        messageBox.setPredefinedButtons(Dialog.PredefinedButton.OK, Dialog.PredefinedButton.CANCEL);
        messageBox.getButton(Dialog.PredefinedButton.OK).addSelectHandler(event -> {
            appCache.loadUpdate();
        });
        messageBox.show();
    }
}
