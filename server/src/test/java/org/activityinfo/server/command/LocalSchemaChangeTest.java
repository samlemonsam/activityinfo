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
package org.activityinfo.server.command;

import com.extjs.gxt.ui.client.data.ModelData;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.fixtures.TestHibernateModule;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.server.database.DatabaseModule;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.endpoint.gwtrpc.GwtRpcModule;
import org.activityinfo.server.mail.MailSenderStubModule;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@Modules({
        MailSenderStubModule.class,
        TestHibernateModule.class,
        GwtRpcModule.class
})
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class LocalSchemaChangeTest extends LocalHandlerTestCase {

    public static final int REPORTING_USER_ID = 2;
    public static final int OWNER_USER_ID = 1;
    public static final int ALPHA_DATABASE_ID = 3;

    @Test
    public void update() throws Exception {
        assertChangeIsSynchronized(3, new UpdateEntity(IndicatorDTO.ENTITY_NAME, 5, newName()));
        assertChangeIsSynchronized(   new UpdateEntity(ActivityDTO.ENTITY_NAME, 2, newName()));
        assertChangeIsSynchronized(1, new UpdateEntity(AttributeGroupDTO.ENTITY_NAME, 1, newName()));
        assertChangeIsSynchronized(1, new UpdateEntity(AttributeDTO.ENTITY_NAME, 1, newName()));
        assertChangeIsSynchronized(   new UpdateEntity(LockedPeriodDTO.ENTITY_NAME, 1, newName()));
        assertChangeIsSynchronized(   new UpdateEntity(UserDatabaseDTO.ENTITY_NAME, 1, newName()));
    }
    
    @Test
    public void updateProject() throws Exception {
        SchemaDTO schema = executeRemotely(new GetSchema());
        ProjectDTO project = schema.getProjectById(1);
        project.setName("New project name!");

        assertChangeIsSynchronized(UpdateEntity.update(project, "name"));
    }

    @Test
    public void delete() throws Exception {
        assertChangeIsSynchronized(3, new Delete(IndicatorDTO.ENTITY_NAME, 5));
        assertChangeIsSynchronized(   new Delete(ActivityDTO.ENTITY_NAME, 2));
        assertChangeIsSynchronized(1, new Delete(AttributeDTO.ENTITY_NAME, 1));
        assertChangeIsSynchronized(1, new Delete(AttributeGroupDTO.ENTITY_NAME, 1)); 
        assertChangeIsSynchronized(   new Delete(LockedPeriodDTO.ENTITY_NAME, 1));
        assertChangeIsSynchronized(   new Delete(ProjectDTO.ENTITY_NAME, 1));
        assertChangeIsSynchronized(   new Delete(UserDatabaseDTO.ENTITY_NAME, 1));
    }

    @Test
    public void updatePartners() throws Exception {
        int databaseId = 1;
        CreateResult createResult = assertChangeIsSynchronized(
                new UpdatePartner(databaseId, new PartnerDTO("Judean People's Front")));
        assertChangeIsSynchronized(new RemovePartner(1, createResult.getNewId()));
    }

    @Test
    public void createActivity() throws Exception {

        SchemaDTO schema = executeRemotely(new GetSchema());

        ActivityFormDTO activity = new ActivityFormDTO();
        activity.setName("New Activity");
        activity.setReportingFrequency(0);
        activity.setLocationType(schema.getLocationTypeById(1));

        assertChangeIsSynchronized(CreateEntity.Activity(schema.getDatabaseById(1), activity));
    }

    @Test
    public void createIndicator() throws Exception {
        
        Map<String, Object> indicator = Maps.newHashMap();
        indicator.put("name", "New Indicator");
        indicator.put("units", "bricks");
        indicator.put("activityId", 2);

        assertChangeIsSynchronized(2, new CreateEntity(IndicatorDTO.ENTITY_NAME, indicator));
    }



    @Test
    public void createAttributes() throws Exception {
        Map<String, Object> attributeGroup = Maps.newHashMap();
        attributeGroup.put("name", "New Indicator");
        attributeGroup.put("activityId", 2);
        attributeGroup.put("multipleAllowed", true);
        attributeGroup.put("mandatory", true);

        CreateResult createResult = assertChangeIsSynchronized(2, new CreateEntity(AttributeGroupDTO.ENTITY_NAME, attributeGroup));


        Map<String, Object> attribute = Maps.newHashMap();
        attribute.put("name", "New Attribute");
        attribute.put("attributeGroupId", createResult.getNewId());

        assertChangeIsSynchronized(2, new CreateEntity(AttributeDTO.ENTITY_NAME, attribute));
    }
    
    @Test
    public void newLocationType() throws Exception {
        Map<String, Object> locationType = Maps.newHashMap();
        locationType.put("name", "Feeding Center");
        locationType.put("databaseId", 2);

        assertChangeIsSynchronized(new CreateEntity("LocationType", locationType));
        
    }
    
    @Test
    public void usersWithViewOnly() throws Exception {
        setUser(2);
        assertLocalSchemaMatchesRemote();
    }
    
    @Test
    public void permissionRevoked() throws Exception {
        setUser(REPORTING_USER_ID);
        synchronize();
        
        setUser(OWNER_USER_ID);
        UserPermissionDTO newPermissions = new UserPermissionDTO();
        newPermissions.setEmail("bavon@nrc.org");
        newPermissions.setAllowView(false);
        newPermissions.setAllowManageUsers(false);
        newPermissions.setAllowEdit(false);
        newPermissions.setPartner(new PartnerDTO(1, "NRC"));
        executeRemotely(new UpdateUserPermissions(1, newPermissions));
        
        setUser(REPORTING_USER_ID);
        assertLocalSchemaMatchesRemote();
    }
    
    @Test
    public void permissionGranted() throws Exception {

        setUser(REPORTING_USER_ID);
        synchronize();

        setUser(OWNER_USER_ID);
        UserPermissionDTO newPermissions = new UserPermissionDTO();
        newPermissions.setEmail("bavon@nrc.org");
        newPermissions.setAllowView(true);
        newPermissions.setPartner(new PartnerDTO(1, "NRC"));
        executeRemotely(new UpdateUserPermissions(ALPHA_DATABASE_ID, newPermissions));

        setUser(REPORTING_USER_ID);
        assertLocalSchemaMatchesRemote();
    }

    private Map<String, Object> newName() {
        Map<String, Object> changes = new HashMap<>();
        changes.put("name", "NEW NAME" + System.currentTimeMillis());
        return changes;
    }
    
    private void assertLocalSchemaMatchesRemote() throws Exception {
        
        synchronize();
        
        SchemaDTO remote = executeRemotely(new GetSchema());
        SchemaDTO local = executeLocally(new GetSchema());

        assertThat(dump(local), equalTo(dump(remote)));

        System.out.println(dump(local));

    }


    private <R extends CommandResult> R assertChangeIsSynchronized(Command<R> command) throws Exception {
        SchemaDTO originalRemote = executeRemotely(new GetSchema());

        synchronize();

        R result = executeRemotely(command);

        SchemaDTO newRemote = executeRemotely(new GetSchema());

        // Verify that the command actually had some effect:
        // otherwise we're not testing anything...
        assertThat(dump(newRemote), not(equalTo(dump(originalRemote))));

        synchronize();

        SchemaDTO newLocal = executeLocally(new GetSchema());

        assertThat(dump(newLocal), equalTo(dump(newRemote)));
        
        System.out.println(dump(newLocal));

        return result;

    }

    private <R extends CommandResult> R assertChangeIsSynchronized(int activityId, Command<R> command) throws Exception {
        ActivityFormDTO originalRemote = executeRemotely(new GetActivityForm(activityId));
        
        synchronize();

        R result = executeRemotely(command);

        ActivityFormDTO newRemote = executeRemotely(new GetActivityForm(activityId));

        // Verify that the command actually had some effect:
        // otherwise we're not testing anything...
        assertThat(dump(newRemote), not(equalTo(dump(originalRemote))));

        synchronize();

        ActivityFormDTO newLocal = executeLocally(new GetActivityForm(activityId));

        assertThat(dump(newLocal), equalTo(dump(newRemote)));

        System.out.println(dump(newLocal));

        return result;
        

    }


    private String dump(ActivityFormDTO object) throws Exception {
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        StringBuilder sb = new StringBuilder();
        dump(sb, visited, "form[" + object.getId() + "]", object);
        return sb.toString();
    }
    
    private String dump(SchemaDTO object) throws Exception {
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        StringBuilder sb = new StringBuilder();
        dump(sb, visited, "countries", object.getCountries());
        dump(sb, visited, "databases", object.getDatabases());
        return sb.toString();
    }

    /**
     * Easiest way to compare these larg-ish objects is to dump them as text...
     */
    private void dump(StringBuilder sb, Set<Object> visited, String prefix, Object modelData) throws Exception {
        if (modelData instanceof ModelData) {
            dumpModelData(sb, visited, prefix, modelData);

        } else if (modelData instanceof Iterable) {
            dumpList(sb, visited, prefix, (Iterable) modelData);
        } else if(
                modelData instanceof Number || 
                modelData instanceof String || 
                modelData instanceof Boolean ||
                modelData instanceof Date || 
                modelData instanceof LocalDate || 
                modelData instanceof Enum || 
                modelData instanceof Extents) {
            sb.append(prefix).append(" = ").append(modelData).append("\n");
        }
    }

    private void dumpModelData(StringBuilder sb, Set<Object> visited, String prefix, Object modelData) throws IntrospectionException {
        if(visited.contains(modelData)) {
            dumpModelReference(sb, prefix, modelData);
        } else {
            visited.add(modelData);
            dumpModelProperties(sb, visited, prefix, modelData);
        }
    }


    private void dumpModelReference(StringBuilder sb, String prefix, Object modelData) {
        sb.append(prefix).append(" = ")
                .append(modelData.getClass().getSimpleName())
                .append("[").append(((ModelData) modelData).<Object>get("id")).append("]\n");
    }

    private void dumpModelProperties(StringBuilder sb, Set<Object> visited, String prefix, Object modelData) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(modelData.getClass());
        for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {

            if (property.getName().equals("billingAccountName") ||
                property.getName().equals("accountEndDate")) {
                continue;
            }

            if (property.getReadMethod() != null &&
                property.getReadMethod().getDeclaringClass().getName().startsWith("org.activityinfo")) {

                try {
                    Object propertyValue = property.getReadMethod().invoke(modelData);
                    dump(sb, visited, prefix + "." + property.getName(), propertyValue);

                } catch (Exception e) {
                    sb.append(prefix).append(".").append(property.getName()).append(" = <")
                            .append(e.getClass().getSimpleName()).append(">\n");
                }
            }
        }
    }


    private void dumpList(StringBuilder sb, Set<Object> visited, String prefix, Iterable modelData) throws Exception {
        List<ModelData> list = Lists.newArrayList(Iterables.filter(modelData, ModelData.class));
        Collections.sort(list, Ordering.natural().onResultOf(input -> input.get("id")));

        for (int i = 0; i != list.size(); ++i) {
            dump(sb, visited, prefix + "[" + list.get(i).get("id") + "]", list.get(i));
        }
    }
}