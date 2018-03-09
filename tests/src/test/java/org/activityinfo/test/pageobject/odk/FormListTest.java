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
package org.activityinfo.test.pageobject.odk;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@SuppressWarnings("unchecked")
public class FormListTest {

    @Test
    public void parse() throws IOException {
        PageSource page = new PageSource(Resources.toString(Resources.getResource("formList.xml"), Charsets.UTF_8));

        List<BlankForm> forms = FormList.parseFormList(page);

        assertThat(forms,
                contains(
                    hasProperty("name", equalTo("Birds")),
                    hasProperty("name", equalTo("Cascading Select Form")),
                    hasProperty("name", equalTo("Cascading Triple Select Form")),
                    hasProperty("name", equalTo("Forest Plot Survey")),
                    hasProperty("name", equalTo("Geo Tagger v2")),
                    hasProperty("name", equalTo("Hypertension Screening"))));
    }
}
