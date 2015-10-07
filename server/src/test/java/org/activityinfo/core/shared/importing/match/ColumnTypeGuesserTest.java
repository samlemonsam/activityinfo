package org.activityinfo.core.shared.importing.match;
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

import org.activityinfo.core.server.type.converter.JvmConverterFactory;
import org.activityinfo.model.type.FieldTypeClass;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author yuriyz on 5/9/14.
 */
public class ColumnTypeGuesserTest {

    @Test
    public void type() {
        assertType(FieldTypeClass.QUANTITY, "1", "2", "3");
        assertType(FieldTypeClass.QUANTITY, "0.1", "2.54", "334");
        assertType(FieldTypeClass.LOCAL_DATE, "2012-12-18");
        assertType(FieldTypeClass.LOCAL_DATE, "2012/12/18");
        assertType(FieldTypeClass.LOCAL_DATE, "2012-12-18 12:38:40.0");
        assertType(FieldTypeClass.FREE_TEXT, "df", "fsdf", "ff");
        assertType(FieldTypeClass.NARRATIVE, "fKholishaur dffsdffsdffsdffsdffsdffsdfdfsdfsdfsdgfdfghdfgddddddddddddddddddddddddddddddddd", "fKholishaur fsdffsdffsdffsdffsdffsdffsdffsdffsdffsdffsdffsdffsdffsdffsdffsdffsdffsdffsdffsdffsdfdffsdffsdffsdffsdffsdffsdfdffsdffsdffsdffsdffsdffsdfdffsdffsdffsdffsdffsdffsdf", "ff");
        assertType(FieldTypeClass.BOOLEAN, "true", "false");
    }

    private void assertType(FieldTypeClass exptectedType, String... columnValueList) {
        List<String> columnValues = Arrays.asList(columnValueList);
        ColumnTypeGuesser guesser = new ColumnTypeGuesser(columnValues, JvmConverterFactory.get());
        final FieldTypeClass formFieldType = guesser.guessType();
        Assert.assertEquals(exptectedType, formFieldType);
    }
}
