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
package org.activityinfo.server.command.handler.sync;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author yuriyz on 03/04/2015.
 */
public class SqlQueryUtilTest {

    @Test
    public void test() {
        Assert.assertEquals(SqlQueryUtil.idSet(Lists.newArrayList(3)), "(3)");
        Assert.assertEquals(SqlQueryUtil.idSet(Lists.newArrayList(1, 3)), "(1,3)");
        Assert.assertEquals(SqlQueryUtil.idSet(Lists.newArrayList(1, 3, 2)), "(1,3,2)");
    }


}
