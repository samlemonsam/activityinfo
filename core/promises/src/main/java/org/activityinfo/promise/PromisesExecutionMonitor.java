package org.activityinfo.promise;
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

import com.google.gwt.thirdparty.guava.common.collect.Lists;

import java.util.List;

/**
 * @author yuriyz on 09/10/2015.
 */
public interface PromisesExecutionMonitor {

    public void onChange(PromisesExecutionStatistic statistic);

    public static class PromisesExecutionStatistic {

        private int completed;
        private int retries;
        private int total;
        private List<PromiseExecutionOperation> notFinishedOperations = Lists.newArrayList();

        public PromisesExecutionStatistic() {
        }

        public PromisesExecutionStatistic(int completed, int retries, int total) {
            this.completed = completed;
            this.retries = retries;
            this.total = total;
        }

        public int getCompleted() {
            return completed;
        }

        public PromisesExecutionStatistic setCompleted(int completed) {
            this.completed = completed;
            return this;
        }

        public int getRetries() {
            return retries;
        }

        public PromisesExecutionStatistic incrementRetry() {
            retries++;
            return this;
        }

        public PromisesExecutionStatistic incrementCompleted() {
            completed++;
            return this;
        }

        public PromisesExecutionStatistic setRetries(int retries) {
            this.retries = retries;
            return this;
        }

        public int getTotal() {
            return total;
        }

        public PromisesExecutionStatistic setTotal(int total) {
            this.total = total;
            return this;
        }

        public List<PromiseExecutionOperation> getNotFinishedOperations() {
            return notFinishedOperations;
        }

        @Override
        public String toString() {
            return "PromisesExecutionStatistic{" +
                    "completed=" + completed +
                    ", retries=" + retries +
                    ", total=" + total +
                    '}';
        }
    }
}
