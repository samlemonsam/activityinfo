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

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.GetReportModel;
import org.activityinfo.legacy.shared.model.ReportDTO;
import org.activityinfo.server.database.OnDataSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;

import static org.junit.Assert.*;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/getreporttests.db.xml")
public class GetReportModelTest extends CommandTestCase {

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(),
            new LocalMemcacheServiceTestConfig());

    @Before
    public void setUp() {
        helper.setUp();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void selectReportOnly() {
        setUser(1);
        ReportDTO result = execute(new GetReportModel(3));
        assertNotNull(result.getReport());
        assertEquals("Report 3", result.getReport().getTitle());
        assertNull(result.getReportMetadataDTO());
    }

    @Test
    public void selectReportOnly2() {
        setUser(1);
        ReportDTO result = execute(new GetReportModel(3, false));
        assertNotNull(result.getReport());
        assertEquals("Report 3", result.getReport().getTitle());
        assertNull(result.getReportMetadataDTO());
    }

    @Test
    public void selectReportWithMetadata() {
        setUser(1);
        ReportDTO result = execute(new GetReportModel(3, true));
        assertNotNull(result.getReport());
        assertEquals("Report 3", result.getReport().getTitle());

        assertNotNull(result.getReportMetadataDTO());
        assertEquals("Alex", result.getReportMetadataDTO().getOwnerName());
    }

    @Test //AI-1359
    public void serializationDeserialization() throws IOException, ClassNotFoundException {
        setUser(1);
        ReportDTO result = execute(new GetReportModel(3, true));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(result);

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ReportDTO rereadDto = (ReportDTO) ois.readObject();

        assertEquals(result.getReportMetadataDTO().getId(), rereadDto.getReportMetadataDTO().getId());
        assertEquals(result.getReportMetadataDTO().getSubscribers(), rereadDto.getReportMetadataDTO().getSubscribers());
        assertEquals(result.getReportMetadataDTO().getAmOwner(), rereadDto.getReportMetadataDTO().getAmOwner());
        assertEquals(result.getReportMetadataDTO().getEmailDelivery(), rereadDto.getReportMetadataDTO().getEmailDelivery());
        assertEquals(result.getReportMetadataDTO().getOwnerName(), rereadDto.getReportMetadataDTO().getOwnerName());
        assertEquals(result.getReportMetadataDTO().getDay(), rereadDto.getReportMetadataDTO().getDay());
    }
}
