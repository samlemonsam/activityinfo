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

import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.result.CommandResult;

import java.util.*;

/**
 * Provides a default, in-memory, command caching implementation based on
 * command equality.
 * <p/>
 * To create a subclass:
 * <p/>
 * 1. make sure the command which you are going to cache implements equals()
 * <u>and</u> hashCode() (press ALT+INS in IntelliJ to generate these methods
 * automatically)
 * <p/>
 * 2. Your subclass constructor should accept Dispatcher as a parameter and use
 * the reference to register itself
 * <p/>
 * 3. Add your subclass to AppModule as an Eager Singleton
 *
 * @author Alex Bertram (akbertram@gmail.com)
 */
public class AbstractCache {

    /**
     * Internal data structure that keeps track of commands and their results,
     * as well as statistics on cache usage that can potentially be used to
     * clean the cache.
     */
    protected class CacheEntry {

        public CacheEntry(CommandResult result) {
            dateCached = new Date();
            hits = 0;
            this.result = result;
        }

        /**
         * The date when the result was received from the server
         */
        private Date dateCached;

        /**
         * The number of times this result has been accessed since being cached.
         */
        private int hits;

        /**
         * The command result
         */
        private CommandResult result;

        public Date getDateCached() {
            return dateCached;
        }

        public int getHits() {
            return hits;
        }

        public CommandResult getResult() {
            return result;
        }

        /**
         * Increments the hit count. See the hits field
         */
        public void hit() {
            hits++;
        }
    }

    /**
     * Maps commands to their results
     */
    private final Map<Command, CacheEntry> results = new HashMap<Command, CacheEntry>();

    /**
     * Tracks the order in which results were cached
     */
    private final List<Command> cache = new LinkedList<Command>();

    /**
     * Adds a command and its result to the cache
     *
     * @param cmd
     * @param result
     */
    protected void cache(Command cmd, CommandResult result) {

        cache.remove(cmd);
        cache.add(cmd);

        results.put(cmd, new CacheEntry(result));
    }

    /**
     * Attempts to retrieve the results of a cached command
     *
     * @param command
     * @return The result originally returned by the server or null if there is
     * no matching cache entry
     */
    protected CommandResult fetch(Command command) {

        CacheEntry entry = results.get(command);
        if (entry != null) {
            entry.hit();
            return entry.getResult();
        } else {
            return null;
        }
    }

    public void clear() {
        cache.clear();
    }
}
