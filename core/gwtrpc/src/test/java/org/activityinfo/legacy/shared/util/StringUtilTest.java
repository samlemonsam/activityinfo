package org.activityinfo.legacy.shared.util;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Strings;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author yuriyz on 10/14/2015.
 */
public class StringUtilTest {

    @Test
    public void truncate() {
        assertEquals(truncateTo(255).length(), 255);
        assertEquals(truncateTo(300).length(), 300);
    }

    public static String truncateTo(int length) {
        return StringUtil.truncate(Strings.padEnd("", 500, 'f'), length);
    }
}
