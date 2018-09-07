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
package org.activityinfo.ui.client.dispatch.remote.cache;

import com.extjs.gxt.ui.client.event.Listener;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.CommandCache;
import org.activityinfo.ui.client.dispatch.DispatchEventSource;
import org.activityinfo.ui.client.dispatch.DispatchListener;
import org.activityinfo.ui.client.offline.sync.SyncCompleteEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class that maintains a list of {@link DispatchListener}s and
 * {@link org.activityinfo.ui.client.dispatch.CommandCache}
 */
@Singleton
public class CacheManager implements DispatchEventSource {

    private Map<Class<? extends Command>, List<DispatchListener>> listeners = new HashMap<Class<? extends Command>,
            List<DispatchListener>>();

    private Map<Class<? extends Command>, List<CommandCache>> proxies = new HashMap<Class<? extends Command>,
            List<CommandCache>>();

    @Inject
    public CacheManager(EventBus eventBus) {
        eventBus.addListener(SyncCompleteEvent.TYPE, new Listener<SyncCompleteEvent>() {

            @Override
            public void handleEvent(SyncCompleteEvent be) {
                clearAllCaches();
            }
        });
    }

    private void clearAllCaches() {
        for (List<CommandCache> list : proxies.values()) {
            for (CommandCache<?> cache : list) {
                cache.clear();
            }
        }
    }

    @Override
    public <T extends Command> void registerListener(Class<T> commandClass, DispatchListener<T> listener) {
        List<DispatchListener> classListeners = listeners.get(commandClass);
        if (classListeners == null) {
            classListeners = new ArrayList<DispatchListener>();
            listeners.put(commandClass, classListeners);
        }
        classListeners.add(listener);
    }

    @Override
    public <T extends Command> void registerProxy(Class<T> commandClass, CommandCache<T> proxy) {
        List<CommandCache> classProxies = proxies.get(commandClass);
        if (classProxies == null) {
            classProxies = new ArrayList<CommandCache>();
            proxies.put(commandClass, classProxies);
        }
        classProxies.add(proxy);
    }

    public void notifyListenersOfSuccess(Command cmd, CommandResult result) {

        List<DispatchListener> classListeners = listeners.get(cmd.getClass());
        if (classListeners != null) {
            for (DispatchListener listener : classListeners) {
                try {
                    listener.onSuccess(cmd, result);
                } catch (Exception e) {
                    Log.error("ProxyManager: listener threw exception during onSuccess notification.", e);
                }
            }
        }
    }

    public void notifyListenersBefore(Command cmd) {

        List<DispatchListener> classListeners = listeners.get(cmd.getClass());
        if (classListeners != null) {
            for (DispatchListener listener : classListeners) {
                try {
                    listener.beforeDispatched(cmd);
                } catch (Exception e) {
                    Log.error("ProxyManager: listener threw exception during beforeCalled notification", e);
                }
            }
        }
    }

    /**
     * Attempts to execute the command locally using one of the registered
     * proxies
     *
     * @param cmd
     * @return
     */
    public CacheResult execute(Command cmd) {

        List<CommandCache> classProxies = proxies.get(cmd.getClass());

        if (classProxies != null) {

            for (CommandCache proxy : classProxies) {

                try {
                    CacheResult r = proxy.maybeExecute(cmd);
                    if (r.isCouldExecute()) {

                        Log.debug("ProxyManager: EXECUTED (!!) " + cmd.toString() + " locally with proxy " +
                                  proxy.getClass().getName());

                        return r;
                    } else {
                        Log.debug("ProxyManager: Failed to execute " + cmd.toString() + " locally with proxy " +
                                  proxy.getClass().getName());

                        return r;
                    }
                } catch (Exception e) {
                    Log.error("ProxyManager: proxy threw exception during call to execute", e);
                }
            }
        }
        return CacheResult.couldNotExecute();
    }
}