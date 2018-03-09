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
package org.activityinfo.test.ui;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import org.activityinfo.test.driver.UiApplicationDriver;
import org.junit.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

import static org.activityinfo.test.driver.Property.name;

/**
 * @author yuriyz on 10/02/2015.
 */
public class ImportSchemaUiTest {

    private static final String DATABASE = "Import-Schema";
    public static final String CVS_FILE_NAME = "import-schema.csv";

    @Inject
    private UiApplicationDriver driver;

    private void background() throws Exception {
        driver.login();
        driver.setup().createDatabase(name(DATABASE));
    }

    private String cvsString() throws IOException {
        final InputStream inputStream = ImportSchemaUiTest.class.getResourceAsStream(CVS_FILE_NAME);
        return new String(ByteStreams.toByteArray(inputStream), Charsets.UTF_8);
    }

    @Test
    public void import500Rows() throws Exception {
        background();

        driver.importSchema(driver.getAliasTable().getAlias(DATABASE), cvsString());
    }
}
