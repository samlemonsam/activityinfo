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
package org.activityinfo.ui.client.component.filter;

import com.google.common.collect.Sets;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.legacy.shared.model.EntityDTO;
import org.activityinfo.legacy.shared.util.Collector;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class AdminTreeProxyTest extends CommandTestCase2 {

    @Test
    public void test() {

        setUser(3);

        AdminTreeProxy proxy = new AdminTreeProxy(getDispatcher());
        proxy.setFilter(Filter.filter().onActivity(2));

        Collector<List<AdminEntityDTO>> collector = new Collector<>();
        proxy.load(null, collector);

        assertThat(collector.getResult(), isSetOf("Ituri", "Sud Kivu"));
        assertThat(collector.getResult(), isSetOf("Ituri", "Sud Kivu"));

    }

    private Matcher<Iterable<? extends EntityDTO>> isSetOf(final String... names) {
        return new TypeSafeMatcher<Iterable<? extends EntityDTO>>() {

            @Override
            protected boolean matchesSafely(Iterable<? extends EntityDTO> list) {
                Set<String> nameSet = Sets.newHashSet(names);
                for(EntityDTO entity : list) {
                    if(!nameSet.remove(entity.getName())) {
                        return false;
                    }
                }
                if(!nameSet.isEmpty()) {
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(Arrays.toString(names));
            }
        };
    }

}
