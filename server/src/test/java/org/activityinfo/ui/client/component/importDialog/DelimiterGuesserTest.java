package org.activityinfo.ui.client.component.importDialog;
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

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.activityinfo.ui.client.component.importDialog.model.source.DelimiterGuesser;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static com.google.common.io.Resources.getResource;

/**
 * @author yuriyz on 10/09/2014.
 */
public class DelimiterGuesserTest {

    @Test
    public void commaDelimiter() throws IOException    {
        String text = Resources.toString(getResource(getClass(), "qis.csv"), Charsets.UTF_8);
        final char delimiter = new DelimiterGuesser(text).guess();

        Assert.assertEquals(delimiter, ',');
    }

    @Test
    public void columnsDoesNotMatch() throws IOException    {
        String text = Resources.toString(getResource(getClass(), "qis-invalid.csv"), Charsets.UTF_8);
        DelimiterGuesser guesser = new DelimiterGuesser(text);
        guesser.guess();

        Assert.assertTrue(guesser.isDataSetOfOneColumn());
        Assert.assertEquals(guesser.getFirstNotMatchedRow(), 2);
    }
}
