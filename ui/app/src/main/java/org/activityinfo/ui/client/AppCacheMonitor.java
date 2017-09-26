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
public class AppCacheMonitor {

    private static final Logger LOGGER = Logger.getLogger(AppCacheMonitor.class.getName());


    private static final int EVERY_FIVE_MINUTES = 5 * 60 * 1000;

    private AppCache appCache;

    /**
     * True if the user has already been prompted during this session to
     * reload for an update.
     */
    private boolean userPrompted;

    public AppCacheMonitor(AppCache appCache) {
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
