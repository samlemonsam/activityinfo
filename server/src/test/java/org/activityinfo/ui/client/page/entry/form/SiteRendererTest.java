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
package org.activityinfo.ui.client.page.entry.form;

import org.activityinfo.legacy.shared.SiteRenderer;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.legacy.shared.type.IndicatorValueFormatter;
import org.activityinfo.model.type.FieldTypeClass;
import org.junit.Before;
import org.junit.Test;

import java.text.DecimalFormat;

import static org.junit.Assert.assertTrue;

public class SiteRendererTest {

    private SiteRenderer siteRenderer;

    private static class JreIndicatorValueFormatter implements IndicatorValueFormatter {
        @Override
        public String format(Double value) {
            return new DecimalFormat("#,##0.####").format(value);
        }
    }


    @Before
    public void setup() {
        siteRenderer = new SiteRenderer(new JreIndicatorValueFormatter());
    }

    @Test
    public void multipleGroupsRender() {


        IndicatorDTO indicator1 = new IndicatorDTO();
        indicator1.setId(1);
        indicator1.setAggregation(IndicatorDTO.AGGREGATE_SUM);
        indicator1.setName("First indicator");
        indicator1.setCategory("First group");

        IndicatorDTO indicator2 = new IndicatorDTO();
        indicator2.setAggregation(IndicatorDTO.AGGREGATE_SUM);
        indicator2.setId(2);
        indicator2.setName("Second indicator");
        indicator2.setCategory("Second group");


        IndicatorDTO indicator3 = new IndicatorDTO();
        indicator3.setAggregation(IndicatorDTO.AGGREGATE_SUM);
        indicator3.setType(FieldTypeClass.NARRATIVE);
        indicator3.setId(3);
        indicator3.setName("Third indicator");
        indicator3.setCategory("Second group");


        ActivityFormDTO activity = new ActivityFormDTO();
        activity.setId(1);
        activity.getIndicators().add(indicator1);
        activity.getIndicators().add(indicator2);
        activity.getIndicators().add(indicator3);

        SiteDTO site = new SiteDTO();
        site.setIndicatorValue(1, 1000d);
        site.setIndicatorValue(2, 2000d);
        site.setIndicatorValue(3, "Some help but not a huge amount.\nHowever there was " +
                                  "some other information that I'd like to present in " +
                                  "second paragraph.");

        String html = siteRenderer.renderSite(site, activity, true);

        System.out.println(html);

        assertTrue(html.contains(indicator1.getName()));
        assertTrue(html.contains(indicator2.getName()));
        assertTrue(html.contains(indicator3.getName()));
    }

}
