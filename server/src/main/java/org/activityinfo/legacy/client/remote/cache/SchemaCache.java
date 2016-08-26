package org.activityinfo.legacy.client.remote.cache;

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
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.activityinfo.legacy.client.CommandCache;
import org.activityinfo.legacy.client.DispatchEventSource;
import org.activityinfo.legacy.client.DispatchListener;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.ActivityFormResults;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Caches the user's schema in-memory for the duration of the session.
 * <p/>
 * TODO: we need to peridiodically check the server for updates. Do we do this
 * here or in a separate class?
 *
 * @author Alex Bertram
 */
public class SchemaCache implements DispatchListener {

    private SchemaDTO schema = null;
    private Set<String> schemaEntityTypes = Sets.newHashSet();
    private Map<Integer, ActivityFormDTO> activityFormCache = Maps.newHashMap();
    private Map<Integer, Integer> indicatorToActivityMap = new HashMap<>();

    @Inject
    public SchemaCache(DispatchEventSource source) {

        initSource(source, this);

        schemaEntityTypes.add("UserDatabase");
        schemaEntityTypes.add("Activity");
        schemaEntityTypes.add("Indicator");
        schemaEntityTypes.add("AttributeGroup");
        schemaEntityTypes.add("AttributeDimension");
        schemaEntityTypes.add("Attribute");
        schemaEntityTypes.add("Partner");
        schemaEntityTypes.add("Project");
        schemaEntityTypes.add("LockedPeriod");
        schemaEntityTypes.add("LocationType");
    }

    public static void initSource(DispatchEventSource source, SchemaCache cache) {
        source.registerProxy(GetSchema.class, cache.new SchemaProxy());
        source.registerProxy(GetActivityForm.class, cache.new ActivityFormProxy());
        source.registerProxy(GetActivityForms.class, cache.new ActivityFormsProxy());
        
        source.registerListener(GetSchema.class, cache);
        source.registerListener(GetActivityForm.class, cache);
        source.registerListener(GetActivityForms.class, cache);
        
        source.registerListener(UpdateEntity.class, cache);
        source.registerListener(CreateEntity.class, cache);
        source.registerListener(AddPartner.class, cache);
        source.registerListener(RemovePartner.class, cache);
        source.registerListener(RequestChange.class, cache);
        source.registerListener(BatchCommand.class, cache);
        source.registerListener(Delete.class, cache);
        source.registerListener(CloneDatabase.class, cache);
    }

    @Override
    public void beforeDispatched(Command command) {
        if (command instanceof UpdateEntity || command instanceof CreateEntity || command instanceof Delete) {
            clearCache();
        } else if (command instanceof CloneDatabase) {
            clearCache();
        } else if (command instanceof Delete && isSchemaEntity(((Delete) command).getEntityName())) {
            clearCache();

        } else if (command instanceof AddPartner || command instanceof RemovePartner) {
            clearCache();

        } else if (command instanceof RequestChange && isSchemaEntity(((RequestChange) command).getEntityType())) {
            clearCache();

        } else if (command instanceof BatchCommand) {
            for (Command element : ((BatchCommand) command).getCommands()) {
                beforeDispatched(element);
            }
        }
    }

    private void clearCache() {
        schema = null;
        activityFormCache.clear();
    }

    private boolean isSchemaEntity(String entityName) {
        return schemaEntityTypes.contains(entityName);
    }

    @Override
    public void onSuccess(Command command, CommandResult result) {
        if (command instanceof GetSchema) {
            cache((SchemaDTO) result);
        
        } else if (command instanceof GetActivityForm) {
            ActivityFormDTO activity = (ActivityFormDTO) result;
            cacheActivityForm(activity);
        
        } else if (command instanceof GetActivityForms) {
            cachActivityForms((ActivityFormResults)result);
            
        } else if (schema != null) {
            if (command instanceof AddPartner) {
                clearCache();
            }
        }
    }

    private void cachActivityForms(ActivityFormResults result) {
        for (ActivityFormDTO form : result.getData()) {
            cacheActivityForm(form);
        }
    }

    private void cacheActivityForm(ActivityFormDTO activity) {
        activityFormCache.put(activity.getId(), activity);
        for (IndicatorDTO indicator : activity.getIndicators()) {
            indicatorToActivityMap.put(indicator.getId(), activity.getId());
        }
    }

    /**
     * Caches the schema in-memory following a successful GetSchema call.
     * Subclasses can override this to provide a more permanent cache.
     *
     * @param schema The schema to cache
     */
    protected void cache(SchemaDTO schema) {
        this.schema = schema;
    }

    @Override
    public void onFailure(Command command, Throwable caught) {
    }
    
    private class ActivityFormsProxy implements CommandCache<GetActivityForms> {

        @Override
        public CacheResult maybeExecute(GetActivityForms command) {
            Set<Integer> activities = Sets.newHashSet(command.getActivities());
            for (Integer indicatorId : command.getIndicators()) {
                // First... do we know to which activity this indicator belongs??
                Integer activityId = indicatorToActivityMap.get(indicatorId);
                if(activityId == null) {
                    return CacheResult.couldNotExecute();
                }
                activities.add(activityId);
            }
            Map<Integer, ActivityFormDTO> forms = new HashMap<>();
            for (Integer activityId :activities) {
                ActivityFormDTO form = activityFormCache.get(activityId);
                if(form == null) {
                    return CacheResult.couldNotExecute();
                }
                forms.put(activityId, form);
            }
            return new CacheResult(new ActivityFormResults(Lists.newArrayList(forms.values())));
        }

        @Override
        public void clear() {
            clearCache();
        }
    }

    private class ActivityFormProxy implements CommandCache<GetActivityForm> {

        @Override
        public CacheResult maybeExecute(GetActivityForm command) {
            if(activityFormCache.containsKey(command.getActivityId())) {
                return new CacheResult(activityFormCache.get(command.getActivityId()));
            }
            return CacheResult.couldNotExecute();
        }

        @Override
        public void clear() {
            clearCache();
        }
    }

    private class SchemaProxy implements CommandCache<GetSchema> {
        @Override
        public CacheResult maybeExecute(GetSchema command) {
            if (schema == null) {
                return CacheResult.couldNotExecute();
            } else {
                return new CacheResult<>(schema);
            }
        }

        @Override
        public void clear() {
            clearCache();
        }
    }
}
