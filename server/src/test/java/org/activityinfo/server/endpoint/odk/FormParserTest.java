package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.fixtures.TestHibernateModule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;

import static com.google.common.io.Resources.getResource;
import static org.junit.Assert.assertEquals;

@RunWith(InjectionSupport.class)
@Modules(TestHibernateModule.class)
public class FormParserTest {

    @Inject
    private FormParser parser;

    @Test
    public void testParseSiteForm() throws Exception {
        String xml = Resources.toString(getResource(FormParserTest.class, "form-instance.xml"), Charsets.UTF_8);

        SiteFormData data = parser.parse(xml);

        assertEquals("uuid:23b56e39-ef50-4510-b85f-c454cd5465c1", data.getInstanceID());
        assertEquals(927, data.getActivity());
        assertEquals(274, data.getPartner());
        assertEquals("Some location", data.getLocationname());
        // assertEquals("", data.getGps());
        assertEquals("2012-07-05", new SimpleDateFormat("yyyy-MM-dd").format(data.getDate1()));
        assertEquals("2013-08-07", new SimpleDateFormat("yyyy-MM-dd").format(data.getDate2()));
        assertEquals("Some comment", data.getComments());

        assertEquals((Double) 52.144802074999994D, (Double) data.getLatitude());
        assertEquals((Double) 5.377899974999999, (Double) data.getLongitude());

        assertEquals(2, data.getIndicators().size());
        assertEquals(4410, data.getIndicators().get(0).getId());
        assertEquals("1.1", data.getIndicators().get(0).getValue());
        assertEquals((Double) 1.1, data.getIndicators().get(0).getDoubleValue());
        assertEquals(4411, data.getIndicators().get(1).getId());
        assertEquals("2.5", data.getIndicators().get(1).getValue());
        assertEquals((Double) 2.5, data.getIndicators().get(1).getDoubleValue());

        assertEquals(1, data.getAttributegroups().size());
        assertEquals(991, data.getAttributegroups().get(0).getId());
        assertEquals("2419 2420", data.getAttributegroups().get(0).getValue());
        assertEquals(2, data.getAttributegroups().get(0).getValueList().size());
        assertEquals((Integer) 2419, data.getAttributegroups().get(0).getValueList().get(0));
        assertEquals((Integer) 2420, data.getAttributegroups().get(0).getValueList().get(1));
    }
}
