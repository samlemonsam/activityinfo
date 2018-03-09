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
package org.activityinfo.test.capacity.load;

import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import org.joda.time.Duration;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LoadExecutor  {

    private static final Logger LOGGER = Logger.getLogger(LoadExecutor.class.getName());

    private ExecutorCompletionService<Void> executorService;

    private Random random = new Random();
    private Function<Duration, Integer> maxConcurrentUsers;

    public LoadExecutor(ExecutorService service) {
        this.executorService = new ExecutorCompletionService<>(service);
    }

    public void setMaxConcurrent(LogisticGrowthFunction function) {
        this.maxConcurrentUsers = function;
    }
    
    public void execute(List<Runnable> tasks) throws InterruptedException {

        LinkedList<Runnable> todo = Lists.newLinkedList(tasks);
        Collections.shuffle(todo);
        
        Stopwatch stopwatch = Stopwatch.createStarted();

        int numActive = 0;
        
        while(todo.size() > 0 || numActive > 0) {

            int maxConcurrent = maxConcurrent(stopwatch);

            while (numActive < maxConcurrent && !todo.isEmpty()) {
                executorService.submit(todo.pop(), null);
                numActive++;
            }

            while(executorService.poll() != null) {
                numActive--;
            }
            Thread.sleep(100);
        }
    }

    private int maxConcurrent(Stopwatch stopwatch) {
        return Math.max(1, maxConcurrentUsers.apply(Duration.millis(stopwatch.elapsed(TimeUnit.MILLISECONDS))));
    }

}