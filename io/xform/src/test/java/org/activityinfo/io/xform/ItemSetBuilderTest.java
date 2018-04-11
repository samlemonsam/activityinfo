package org.activityinfo.io.xform;

import com.google.common.base.Charsets;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.store.query.server.FormSourceSyncImpl;
import org.activityinfo.store.testing.TestingStorageProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ItemSetBuilderTest {

    private FormSourceSyncImpl formSource;
    private TestingStorageProvider catalog;

    @Before
    public void setup() {
        LocaleProxy.initialize();

        catalog = new TestingStorageProvider();
        formSource = new FormSourceSyncImpl(catalog, 1);
    }

    @Test
    public void villageForm() throws IOException {


        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ItemSetBuilder builder = new ItemSetBuilder(formSource, catalog.getVillageForm().getFormId(), baos);

        System.out.println(new String(baos.toByteArray(), Charsets.UTF_8));



    }

}