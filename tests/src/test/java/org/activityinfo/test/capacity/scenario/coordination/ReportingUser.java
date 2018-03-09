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
package org.activityinfo.test.capacity.scenario.coordination;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.test.capacity.action.CompositeAction;
import org.activityinfo.test.capacity.action.Sampling;
import org.activityinfo.test.capacity.action.SynchronizeAction;
import org.activityinfo.test.capacity.action.UserAction;
import org.activityinfo.test.capacity.model.UserRole;
import org.activityinfo.test.driver.ApiApplicationDriver;
import org.activityinfo.test.driver.FieldValue;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;


public class ReportingUser implements UserRole {
    
    private static final int INSTANCES_PER_REPORT = 10;
    
    private final PartnerOrganization organization;
    private final String nickname;
    private Sector sector;
    private ConcurrentMap<String, String> localVersions = new ConcurrentHashMap<>();
    
    private UserAction reportingAction;
    private SynchronizeAction synchronizeAction;


    public ReportingUser(PartnerOrganization organization, String nickname, Sector sector) {
        this.organization = organization;
        this.nickname = nickname;
        this.sector = sector;
        this.reportingAction = new ReportResults();
        synchronizeAction = new SynchronizeAction();
    }

    @Override
    public String getNickName() {
        return nickname;
    }

    @Override
    public Optional<UserAction> getTask(int dayNumber) {
        if(dayNumber > 1) {
            return Optional.<UserAction>of(new CompositeAction(synchronizeAction, reportingAction));
        } else {
            return Optional.absent();
        }
    }

    private class ReportResults implements UserAction {

        @Override
        public String toString() {
            return sector.getSectorName() + " Sector Reporting";
        }

        @Override
        public void execute(ApiApplicationDriver driver) throws Exception {
            
            List<String> forms = driver.getForms(sector.getDatabaseName());
            if(!forms.isEmpty()) {
                String form = Sampling.chooseOne(forms);

                driver.startBatch();

                for(int submissionIndex=0;submissionIndex<INSTANCES_PER_REPORT;++submissionIndex) {
                    List<FieldValue> values = Lists.newArrayList();
                    // Partner
                    values.add(new FieldValue("partner", organization.getName()));

                    // Create random values for indicators
                    for(int indicatorIndex=0;indicatorIndex<sector.getIndicatorCount();++indicatorIndex) {
                        String fieldName = sector.getIndicatorName(form, indicatorIndex);
                        int value = ThreadLocalRandom.current().nextInt(0, 1000);
                        
                        values.add(new FieldValue(fieldName, value));
                    }
                    driver.submitForm(form, values);
                    driver.flush();
                }
                driver.submitBatch();
            }
        }
    }
    
}
