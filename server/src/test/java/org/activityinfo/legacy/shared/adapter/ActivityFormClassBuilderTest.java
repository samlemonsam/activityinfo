package org.activityinfo.legacy.shared.adapter;

import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.GetSites;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.activityinfo.core.client.PromiseMatchers.assertResolves;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
public class ActivityFormClassBuilderTest extends CommandTestCase2 {

    public static final int BAVON_USER_ID = 2;

    @Test @OnDataSet("/dbunit/sites-simple1.db.xml")
    public void partnersFieldIsNotVisibleIfThereIsOnlyOneChoice() {

        setUser(BAVON_USER_ID);

        ResourceLocatorAdaptor locator = new ResourceLocatorAdaptor(getDispatcher());

        FormClass formClass = assertResolves(locator.getFormClass(CuidAdapter.activityFormClass(1)));

        ResourceId partnerFieldId = CuidAdapter.field(formClass.getId(), CuidAdapter.PARTNER_FIELD);
        FormField partnerField = formClass.getField(partnerFieldId);

        assertThat(partnerField, hasProperty("visible", equalTo(false)));

        // Make sure we can update if partner is not specified
        FormInstance instance = new FormInstance(CuidAdapter.newLegacyFormInstanceId(formClass.getId()), formClass.getId());
        instance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.START_DATE_FIELD), new LocalDate(2014, 1, 1));
        instance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.END_DATE_FIELD), new LocalDate(2014, 1, 2));
        instance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.LOCATION_FIELD),
                new ReferenceValue(CuidAdapter.locationInstanceId(1)));

        assertResolves(locator.persist(instance));
    }

    @Test @OnDataSet("/dbunit/chad-form.db.xml")
    public void nullLocationTypeIsNotVisible() {

        setUser(9944);

        ResourceLocatorAdaptor locator = new ResourceLocatorAdaptor(getDispatcher());

        FormClass formClass = assertResolves(locator.getFormClass(CuidAdapter.activityFormClass(11218)));

        ResourceId locationFieldId = CuidAdapter.field(formClass.getId(), CuidAdapter.LOCATION_FIELD);
        assertThat(formClass.getFields(), not(hasItem(withId(locationFieldId))));


        // Make sure we can update if location is not specified
        FormInstance instance = new FormInstance(CuidAdapter.newLegacyFormInstanceId(formClass.getId()), formClass.getId());
        instance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.START_DATE_FIELD), new LocalDate(2014, 1, 1));
        instance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.END_DATE_FIELD), new LocalDate(2014, 1, 2));

        assertResolves(locator.persist(instance));

        // Make sure the null location object is visible to legacy code
        SiteDTO site = execute(GetSites.byId(CuidAdapter.getLegacyIdFromCuid(instance.getId()))).getData().get(0);
        assertThat(site.getLocationName(), equalTo("Chad"));
    }

    private Matcher<FormField> withId(ResourceId locationFieldId) {
        return Matchers.<FormField>hasProperty("id", equalTo(locationFieldId));
    }
}