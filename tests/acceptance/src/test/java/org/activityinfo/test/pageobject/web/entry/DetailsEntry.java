package org.activityinfo.test.pageobject.web.entry;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.test.driver.FieldValue;
import org.activityinfo.test.driver.Indicator;
import org.junit.Assert;

import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 04/17/2015.
 */
public class DetailsEntry {

    private final List<Indicator> indicators = Lists.newArrayList();

    public DetailsEntry() {
    }

    public List<Indicator> getIndicators() {
        return indicators;
    }

    public Map<String, Indicator> indicatorMapByName() {
        final Map<String, Indicator> map = Maps.newHashMap();
        for (Indicator indicator : indicators) {
            map.put(indicator.getName(), indicator);
        }
        return map;
    }

    public void assertVisible(List<FieldValue> values) {
        Map<String, Indicator> map = indicatorMapByName();

        for (FieldValue value : values) {
            Indicator indicator = map.get(value.getField());

            Assert.assertNotNull("Indicator is not visible, name: " + value.getField(), indicator);
            Assert.assertEquals("Value for indicator with name: " + value.getField() + " does not match.", value.getValue(), indicator.getValue());
        }
    }
}
