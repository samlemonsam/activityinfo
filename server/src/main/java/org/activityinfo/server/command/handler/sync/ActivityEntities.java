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

import com.google.api.client.util.Sets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.server.database.hibernate.entity.*;

import javax.persistence.EntityManager;
import java.util.*;

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
    private final int dbId;

    public ActivityEntities(EntityManager entityManager, int dbId) {
        this.entityManager = entityManager;
        this.dbId = dbId;
    }

    public void collect(ActivityEntities existingActivityEntities) {
        allLockedPeriods.addAll(existingActivityEntities.getAllLockedPeriods());
        indicators.putAll(existingActivityEntities.getIndicators());
        indicatorLinks.addAll(existingActivityEntities.getIndicatorLinks());
        attributeGroups.putAll(existingActivityEntities.getAttributeGroups());
        attributes.putAll(existingActivityEntities.getAttributes());
        activitiesMap.putAll(existingActivityEntities.getActivitiesMap());
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

    private void findIndicatorLinks() {
        List<Object[]> result = entityManager.createNativeQuery(
                "select link.sourceindicatorid, link.destinationindicatorid from indicatorlink link " +
                        "left join indicator i on (link.destinationindicatorid=i.indicatorid or link.sourceindicatorid=i.indicatorid) " +
                        "left join activity a on (i.activityid=a.activityid) " +
                        "where a.databaseId = " + dbId)
                .getResultList();
        Set<IndicatorLinkEntityId> links = Sets.newHashSet(); // avoid duplications
        for (Object[] obj : result) {
            links.add(new IndicatorLinkEntityId((Integer) obj[0], (Integer) obj[1]));
        }
        for (IndicatorLinkEntityId link : links) {
            indicatorLinks.add(new IndicatorLinkEntity(link));
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
