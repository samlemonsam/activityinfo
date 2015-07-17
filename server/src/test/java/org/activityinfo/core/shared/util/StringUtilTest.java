package org.activityinfo.core.shared.util;
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

import org.activityinfo.model.expr.StringUtil;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.activityinfo.model.expr.StringUtil.getLevenshteinDistance;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author yuriyz on 5/7/14.
 */
public class StringUtilTest {

    @Test
    public void isAlphabetic() {
        assertThat(StringUtil.isAlphabetic('a'), Matchers.equalTo(true));
        assertThat(StringUtil.isAlphabetic('A'), Matchers.equalTo(true));
        assertThat(StringUtil.isAlphabetic('1'), Matchers.equalTo(true));
        assertThat(StringUtil.isAlphabetic('4'), Matchers.equalTo(true));
        assertThat(StringUtil.isAlphabetic('_'), Matchers.equalTo(false));
        assertThat(StringUtil.isAlphabetic('*'), Matchers.equalTo(false));
    }

    @Test
    public void testLevenshteinDistance() {
        assertThat(getLevenshteinDistance("a", "b"), equalTo(1));
        assertThat(getLevenshteinDistance("ab", "bb"), equalTo(1));
        assertThat(getLevenshteinDistance("ab ", " bb"), equalTo(2));
    }
}
