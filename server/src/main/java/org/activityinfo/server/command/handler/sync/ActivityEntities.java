package org.activityinfo.server.command.handler.sync;
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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.server.database.hibernate.entity.*;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author yuriyz on 03/04/2015.
 */
public class ActivityEntities {

    private final List<LockedPeriod> allLockedPeriods = Lists.newArrayList();
    private final Map<Integer, Indicator> indicators = Maps.newHashMap();
    private final Set<IndicatorLinkEntity> indicatorLinks = new HashSet<>();

    private final Map<Integer, AttributeGroup> attributeGroups = Maps.newHashMap();
    private final Map<Integer, Attribute> attributes = Maps.newHashMap();
    private final Map<Integer, Activity> activitiesMap = Maps.newHashMap();

    private final EntityManager entityManager;

    public ActivityEntities(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void collect(Collection<Activity> activities) {
        for (Activity activity : activities) {
            activitiesMap.put(activity.getId(), activity);

            allLockedPeriods.addAll(activity.getLockedPeriods());

            for (Indicator indicator : activity.getIndicators()) {
                indicators.put(indicator.getId(), indicator);
            }

            for (AttributeGroup g : activity.getAttributeGroups()) {
                if (!attributeGroups.containsKey(g.getId())) {
                    attributeGroups.put(g.getId(), g);

                    for (Attribute a : g.getAttributes()) {
                        attributes.put(a.getId(), a);
                    }
                }
            }
        }

        findIndicatorLinks();
    }

    @SuppressWarnings("unchecked") // query indicator links with one call
    private void findIndicatorLinks() {
        if (indicators.isEmpty()) {// nothing to handle
            return;
        }

        List<Integer> indicatorIdList = Lists.newArrayList(indicators.keySet());

        // todo remove later : experiment - read by chunks?
//        if (indicatorIdList.size() > 1000) {
//            indicatorIdList = indicatorIdList.subList(0, 1000); // only first 1000 indicators
//        }

        Stopwatch stopwatch = Stopwatch.createStarted(); // we may get timeout here if indicatorList.size() > 10000
        List<IndicatorLinkEntity> result = entityManager.createQuery(
                "select il from IndicatorLinkEntity il where il.id.sourceIndicatorId in (:sourceId) or il.id" +
                        ".destinationIndicatorId in (:destId)")
                .setParameter("sourceId", indicatorIdList)
                .setParameter("destId", indicatorIdList)
                .getResultList();
        if (result != null && !result.isEmpty()) {
            Log.info("Fetching of  for IndicatorLinkEntity with " + indicatorIdList.size() + " inticators takes " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
            indicatorLinks.addAll(result);
        }
    }

    public List<LockedPeriod> getAllLockedPeriods() {
        return allLockedPeriods;
    }

    public Map<Integer, Indicator> getIndicators() {
        return indicators;
    }

    public Set<IndicatorLinkEntity> getIndicatorLinks() {
        return indicatorLinks;
    }

    public Map<Integer, AttributeGroup> getAttributeGroups() {
        return attributeGroups;
    }

    public Map<Integer, Attribute> getAttributes() {
        return attributes;
    }

    public Map<Integer, Activity> getActivitiesMap() {
        return activitiesMap;
    }
}
