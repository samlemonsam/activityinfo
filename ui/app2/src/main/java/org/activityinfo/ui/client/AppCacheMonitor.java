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

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.Window;
import org.activityinfo.i18n.shared.I18N;
import org.realityforge.gwt.appcache.client.ApplicationCache;
import org.realityforge.gwt.appcache.client.event.UpdateReadyEvent;

import javax.annotation.Nonnull;

public class AppCacheMonitor {

    public static void start() {
        ApplicationCache applicationCache = ApplicationCache.getApplicationCacheIfSupported();
        if(applicationCache == null) {
            return;
        }
        applicationCache.addUpdateReadyHandler(new UpdateReadyEvent.Handler() {
            @Override
            public void onUpdateReadyEvent(@Nonnull UpdateReadyEvent event) {
                MessageBox.confirm(I18N.MESSAGES.newVersion(ClientContext.getAppTitle()),
                        I18N.CONSTANTS.newVersionChoice(),
                        new Listener<MessageBoxEvent>() {

                            @Override
                            public void handleEvent(MessageBoxEvent be) {
                                if (be.getButtonClicked().getItemId()
                                        .equals(Dialog.YES)) {
                                    Window.Location.reload();
                                }
                            }
                        });
            }
        });
    }

}
