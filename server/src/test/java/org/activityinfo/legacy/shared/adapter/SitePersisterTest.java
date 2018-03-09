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

import com.google.common.collect.Lists;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.activityinfo.promise.PromiseMatchers.assertResolves;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class SitePersisterTest extends CommandTestCase2 {

    private static final int DATABASE_ID = 1;
    private static final ResourceId PEAR_Activity = CuidAdapter.activityFormClass(1);
    private static final ResourceId CONTENU_DI_KIT_FIELD = CuidAdapter.attributeGroupField(2);
    private static final ResourceId CONTENU_DI_KIT_FIELD_ATTR_VALUE = CuidAdapter.attributeId(3);


    @Test
    public void test() {
        FormClass formClass = assertResolves(locator.getFormClass(PEAR_Activity));

        FormInstance siteFormInstance = new FormInstance(CuidAdapter.generateSiteCuid(), formClass.getId());
        siteFormInstance.set(CONTENU_DI_KIT_FIELD, new EnumValue(CONTENU_DI_KIT_FIELD_ATTR_VALUE));

        // built-in values
        siteFormInstance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.PARTNER_FIELD), 
                new ReferenceValue(
                        new RecordRef(
                                CuidAdapter.partnerFormId(DATABASE_ID),
                                CuidAdapter.partnerRecordId(1))));
        siteFormInstance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.START_DATE_FIELD), new LocalDate(2014, 1, 1));
        siteFormInstance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.END_DATE_FIELD), new LocalDate(2014, 1, 2));
        siteFormInstance.set(CuidAdapter.field(formClass.getId(), CuidAdapter.LOCATION_FIELD),
                new ReferenceValue(
                        new RecordRef(
                            CuidAdapter.locationFormClass(1),
                            CuidAdapter.locationInstanceId(1))));

        assertResolves(locator.persist(siteFormInstance));

        // query by id
        FormInstance fromServer = assertResolves(locator.getFormInstance(siteFormInstance.getFormId(), siteFormInstance.getId()));
        Assert.assertNotNull(fromServer);
        Assert.assertEquals(fromServer.get(CONTENU_DI_KIT_FIELD), new EnumValue(CONTENU_DI_KIT_FIELD_ATTR_VALUE));

        FormTree formTree = assertResolves(locator.getFormTree(formClass.getId()));

        final List<FieldPath> paths = Lists.newArrayList(formTree.getRootPaths());
        Assert.assertTrue(paths.contains(new FieldPath(CONTENU_DI_KIT_FIELD)));

        // query projection
        FormInstance instance = assertResolves(locator.getFormInstance(formClass.getId(), siteFormInstance.getId()));
        EnumValue fieldValue = (EnumValue) instance.get(CONTENU_DI_KIT_FIELD);
        assertThat(fieldValue.getValueId(), equalTo(CONTENU_DI_KIT_FIELD_ATTR_VALUE));
    }

}