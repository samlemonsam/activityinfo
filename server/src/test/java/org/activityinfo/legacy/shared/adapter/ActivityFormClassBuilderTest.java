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
package org.activityinfo.legacy.shared.adapter;

import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.GetSites;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.activityinfo.promise.PromiseMatchers.assertResolves;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
public class ActivityFormClassBuilderTest extends CommandTestCase2 {

    public static final int BAVON_USER_ID = 2;

    @Test @OnDataSet("/dbunit/sites-simple1.db.xml")
    public void partnersFieldIsAlwaysVisible() {

        setUser(BAVON_USER_ID);

        FormClass formClass = assertResolves(locator.getFormClass(CuidAdapter.activityFormClass(1)));
        int databaseId = 1;

        ResourceId partnerFieldId = CuidAdapter.field(formClass.getId(), CuidAdapter.PARTNER_FIELD);
        FormField partnerField = formClass.getField(partnerFieldId);

        assertThat(partnerField, hasProperty("visible", equalTo(true))); // according to ai-1009 : partner field is always visible

        // Make sure we can update if partner is not specified
        FormInstance instance = new FormInstance(CuidAdapter.newLegacyFormInstanceId(formClass.getId()), formClass.getId());
        instance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.START_DATE_FIELD), new LocalDate(2014, 1, 1));
        instance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.END_DATE_FIELD), new LocalDate(2014, 1, 2));
        instance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.LOCATION_FIELD),
                new ReferenceValue(
                        new RecordRef(
                            CuidAdapter.locationFormClass(1),
                            CuidAdapter.locationInstanceId(1))));
        instance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.PARTNER_FIELD), CuidAdapter.partnerRef(databaseId, 1));

        assertResolves(locator.persist(instance));
    }

    @Test @OnDataSet("/dbunit/chad-form.db.xml")
    public void nullLocationTypeIsNotVisible() {

        setUser(9944);

        int databaseId = 1470;
        FormClass formClass = assertResolves(locator.getFormClass(CuidAdapter.activityFormClass(11218)));

        ResourceId locationFieldId = CuidAdapter.field(formClass.getId(), CuidAdapter.LOCATION_FIELD);
        assertThat(formClass.getFields(), not(hasItem(withId(locationFieldId))));


        // Make sure we can update if location is not specified
        FormInstance instance = new FormInstance(CuidAdapter.newLegacyFormInstanceId(formClass.getId()), formClass.getId());
        instance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.START_DATE_FIELD), new LocalDate(2014, 1, 1));
        instance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.END_DATE_FIELD), new LocalDate(2014, 1, 2));
        instance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.PARTNER_FIELD), CuidAdapter.partnerRef(databaseId, 1734));
        instance.set(ResourceId.valueOf("Q0000031845"), new EnumValue(CuidAdapter.attributeField(166617)));

        assertResolves(locator.persist(instance));

        // Make sure the null location object is visible to legacy code
        SiteDTO site = execute(GetSites.byId(CuidAdapter.getLegacyIdFromCuid(instance.getId()))).getData().get(0);
        assertThat(site.getLocationName(), equalTo("Chad"));
    }

    private Matcher<FormField> withId(ResourceId locationFieldId) {
        return Matchers.<FormField>hasProperty("id", equalTo(locationFieldId));
    }
}