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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.test.capacity.CapacityTest;
import org.activityinfo.test.capacity.TestContext;
import org.activityinfo.test.capacity.action.ActionExecution;
import org.activityinfo.test.capacity.action.UserAction;
import org.activityinfo.test.capacity.model.Scenario;
import org.activityinfo.test.capacity.model.ScenarioContext;
import org.activityinfo.test.capacity.model.UserRole;
import org.joda.time.Period;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class ScenarioRun implements Runnable {

    private static Logger LOGGER = Logger.getLogger(Scenario.class.getName());

    private final ScenarioContext context;
    private ExecutorService executorService;
    private Scenario scenario;

    public ScenarioRun(TestContext testContext, ExecutorService executorService, Scenario scenario) {
        this.executorService = executorService;
        this.scenario = scenario;
        this.context = new ScenarioContext(testContext);
    }

    @Override
    public void run() {
        int dayNumber = 0;

        try {
            while (dayNumber < scenario.getDayCount()) {
                run(dayNumber);
                dayNumber++;
            }
        } catch (InterruptedException e) {
            LOGGER.severe(String.format("%s: Interrupted.", e));
        }
    }

    private void run(int dayNumber) throws InterruptedException {

        List<Runnable> tasks = Lists.newArrayList();

        // Enumerate all the tasks that users need to accomplish today
        for(UserRole user : scenario.getUsers()) {
            Optional<UserAction> task = user.getTask(dayNumber);
            if (task.isPresent()) {
                tasks.add(new ActionExecution(context, user, task.get()));
            }
        }

        // Randomize queue priority
        Collections.shuffle(tasks);

        LOGGER.info(String.format("%s: Day %d starting...", scenario.toString(), dayNumber));

        // Enqueue and wait for all users to finish
        LoadExecutor loadExecutor = new LoadExecutor(executorService);
        loadExecutor.setMaxConcurrent(LogisticGrowthFunction.rampUpTo(CapacityTest.MAX_CONCURRENT_USERS).during(Period.seconds(90)));
        loadExecutor.execute(tasks);
        
        LOGGER.info(String.format("%s: Run complete.", scenario.toString()));
    }
}
