package org.activityinfo.model.type.subform;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.period.PredefinedPeriods;

import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 01/27/2015.
 */
public class SubFormTypeRegistry {

    private static final SubFormTypeRegistry INSTANCE = new SubFormTypeRegistry();

    private final Map<ResourceId, SubFormType> types = Maps.newLinkedHashMap();

    private SubFormTypeRegistry() {

        register(ClassType.REPEATING.createSubformKind());

        register(new PeriodSubFormType(PredefinedPeriods.YEARLY));
        register(new PeriodSubFormType(PredefinedPeriods.MONTHLY));
        register(new PeriodSubFormType(PredefinedPeriods.BI_WEEKLY));
        register(new PeriodSubFormType(PredefinedPeriods.WEEKLY));
        register(new PeriodSubFormType(PredefinedPeriods.DAILY));

    }

    private void register(SubFormType kind) {
        types.put(kind.getDefinition().getId(), kind);
    }

    public static SubFormTypeRegistry get() {
        return INSTANCE;
    }

    public SubFormType getType(String id) {
        return getType(ResourceId.valueOf(id));
    }

    public SubFormType getType(ResourceId id) {
        return types.get(id);
    }

    public List<SubFormType> getTypes() {
        return Lists.newArrayList(types.values());
    }
}
