package org.activityinfo.legacy.shared.adapter;

import com.google.common.collect.Lists;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.AsyncFormTreeBuilder;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.activityinfo.core.client.PromiseMatchers.assertResolves;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class SitePersisterTest extends CommandTestCase2 {

    private static final ResourceId PEAR_Activity = CuidAdapter.activityFormClass(1);
    private static final ResourceId CONTENU_DI_KIT_FIELD = CuidAdapter.attributeGroupField(2);
    private static final ResourceId CONTENU_DI_KIT_FIELD_ATTR_VALUE = CuidAdapter.attributeId(3);


    @Test
    public void test() {
        FormClass formClass = assertResolves(locator.getFormClass(PEAR_Activity));

        FormInstance siteFormInstance = new FormInstance(CuidAdapter.generateSiteCuid(), formClass.getId());
        siteFormInstance.set(CONTENU_DI_KIT_FIELD, CONTENU_DI_KIT_FIELD_ATTR_VALUE);

        // built-in values
        siteFormInstance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.PARTNER_FIELD), 
                new ReferenceValue(CuidAdapter.partnerInstanceId(1)));
        siteFormInstance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.START_DATE_FIELD), new LocalDate(2014, 1, 1));
        siteFormInstance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.END_DATE_FIELD), new LocalDate(2014, 1, 2));
        siteFormInstance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.LOCATION_FIELD),
                new ReferenceValue(CuidAdapter.locationInstanceId(1)));

        assertResolves(locator.persist(siteFormInstance));

        // query by id
        FormInstance fromServer = assertResolves(locator.getFormInstance(siteFormInstance.getClassId(), siteFormInstance.getId()));
        Assert.assertNotNull(fromServer);
        Assert.assertEquals(fromServer.get(CONTENU_DI_KIT_FIELD), new EnumValue(CONTENU_DI_KIT_FIELD_ATTR_VALUE));

        FormTree formTree = assertResolves(new AsyncFormTreeBuilder(locator).apply(formClass.getId()));

        final List<FieldPath> paths = Lists.newArrayList(formTree.getRootPaths());
        Assert.assertTrue(paths.contains(new FieldPath(CONTENU_DI_KIT_FIELD)));

        // query projection
        FormInstance instance = assertResolves(locator.getFormInstance(formClass.getId(), siteFormInstance.getId()));
        EnumValue fieldValue = (EnumValue) instance.get(CONTENU_DI_KIT_FIELD);
        assertThat(fieldValue.getValueId(), equalTo(CONTENU_DI_KIT_FIELD_ATTR_VALUE));
    }

}