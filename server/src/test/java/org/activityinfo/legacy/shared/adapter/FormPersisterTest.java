package org.activityinfo.legacy.shared.adapter;

import com.google.inject.Inject;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.activityinfo.core.client.PromiseMatchers.assertResolves;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/chad-form.db.xml")
public class FormPersisterTest extends CommandTestCase2 {

    @Inject
    private TestingResourceLocatorAdapter locator;

    @Before
    public void setUp() throws Exception {
        setUser(9944);
    }

    @Test
    public void noDuplicates() {

        FormClass formClass = assertResolves(locator.getFormClass(CuidAdapter.activityFormClass(11218)));
        assertResolves(locator.persist(formClass));

        FormClass formClass2 = assertResolves(locator.getFormClass(CuidAdapter.activityFormClass(11218)));
        
        System.out.println(formClass.getFields());
        System.out.println(formClass2.getFields());


        assertThat(formClass2.getFields().size(), equalTo(formClass.getFields().size()));
    }

    @Test
    public void barcodes() {

        FormClass formClass = assertResolves(locator.getFormClass(CuidAdapter.activityFormClass(11218)));

        ResourceId barcodeId = ResourceId.generateFieldId(BarcodeType.TYPE_CLASS);
        formClass.addElement(new FormField(barcodeId)
            .setLabel("HH ID")
            .setType(BarcodeType.INSTANCE)
            .setVisible(true));
        assertResolves(locator.persist(formClass));


        FormClass formClass2 = assertResolves(locator.getFormClass(CuidAdapter.activityFormClass(11218)));


        FormField field = findFieldByLabel(formClass2.getFields(), "HH ID");
        assertThat(field.getType(), instanceOf(BarcodeType.class));


    }

    private FormField findFieldByLabel(List<FormField> fields, String s) {
        for(FormField field : fields) {
            System.out.println(field);
            if(field.getLabel().equals(s)) {
                return field;
            }
        }
        throw new IllegalArgumentException(s);
    }


}