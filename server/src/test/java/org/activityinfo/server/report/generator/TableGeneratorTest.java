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
package org.activityinfo.server.report.generator;

import junit.framework.Assert;
import org.activityinfo.legacy.shared.command.GetBaseMaps;
import org.activityinfo.legacy.shared.command.GetSites;
import org.activityinfo.legacy.shared.command.result.BaseMapResult;
import org.activityinfo.legacy.shared.command.result.SiteResult;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.legacy.shared.model.TileBaseMap;
import org.activityinfo.legacy.shared.reports.content.BubbleMapMarker;
import org.activityinfo.legacy.shared.reports.content.MapContent;
import org.activityinfo.legacy.shared.reports.content.TableData;
import org.activityinfo.legacy.shared.reports.model.MapReportElement;
import org.activityinfo.legacy.shared.reports.model.TableColumn;
import org.activityinfo.legacy.shared.reports.model.TableElement;
import org.activityinfo.legacy.shared.reports.model.labeling.ArabicNumberSequence;
import org.activityinfo.legacy.shared.reports.model.layers.BubbleMapLayer;
import org.activityinfo.legacy.shared.reports.model.layers.CircledMapLayer;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.hibernate.entity.User;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Alex Bertram
 */
public class TableGeneratorTest {
    private static final int INDICATOR_ID = 99;
    private User user;

    @Before
    public void setUp() {
        user = new User();
        user.setName("Alex");
        user.setEmail("akbertra@mgail.com");
        user.setLocale("fr");
    }

    @Test
    public void simpleTable() {

        TableElement table = new TableElement();
        TableColumn column = new TableColumn("Location", "location.name");
        table.addColumn(column);

        TableGenerator gtor = new TableGenerator(createDispatcher(), null);
        gtor.generate(user, table, null, null);

        Assert.assertNotNull("content is set", table.getContent());

        TableData data = table.getContent().getData();
        List<SiteDTO> rows = data.getRows();
        Assert.assertEquals("row count", 1, rows.size());

        SiteDTO row = rows.get(0);
        assertThat((String) row.get(column.getSitePropertyName()),
                equalTo("tampa bay"));
    }

    //
    // @Test
    // public void tableWithMap() {
    //
    // MapReportElement map = new MapReportElement();
    // map.setBaseMapId(GoogleBaseMap.ROADMAP.getId());
    //
    // BubbleMapLayer layer = new BubbleMapLayer();
    // layer.addIndicator(INDICATOR_ID);
    // map.addLayer(layer);
    //
    // TableElement table = new TableElement();
    // table.setMap(map);
    //
    // TableColumn column = new TableColumn("Location", "location.name");
    // table.addColumn(column);
    //
    // TableColumn mapColumn = new TableColumn("Map", "map");
    // table.addColumn(mapColumn);
    //
    // DispatcherSync dispatcher = createDispatcher();
    // TableGenerator gtor = new TableGenerator(dispatcher, new
    // MapGenerator(dispatcher, null, null));
    // gtor.generate(user, table, null, null);
    //
    // Assert.assertNotNull("content is set", table.getContent());
    //
    // TableData data = table.getContent().getData();
    // List<SiteDTO> rows = data.getRows();
    // Assert.assertEquals("row count", 1, rows.size());
    //
    // SiteDTO row = rows.get(0);
    // assertThat((String)row.get(column.getSitePropertyName()),
    // equalTo("tampa bay"));
    // assertThat((String)row.get("map"), equalTo("1"));
    // }

    @Test
    public void testMap() {

        TableElement table = new TableElement();
        table.addColumn(new TableColumn("Index", "map"));
        table.addColumn(new TableColumn("Site", "location.name"));

        MapReportElement map = new MapReportElement();
        map.setBaseMapId("map1");
        CircledMapLayer layer = new BubbleMapLayer();
        layer.setLabelSequence(new ArabicNumberSequence());
        map.addLayer(layer);
        table.setMap(map);

        DispatcherSync dispatcher = createMock(DispatcherSync.class);
        expect(dispatcher.execute(isA(GetSites.class)))
                .andReturn(new SiteResult(dummySite()))
                .anyTimes();

        TileBaseMap baseMap1 = new TileBaseMap();
        baseMap1.setId("map1");
        baseMap1.setMinZoom(0);
        baseMap1.setMaxZoom(12);
        baseMap1.setCopyright("(C)");
        baseMap1.setName("Grand Canyon");
        baseMap1.setTileUrlPattern("http://s/test.png");

        expect(dispatcher.execute(isA(GetBaseMaps.class)))
                .andReturn(new BaseMapResult(Collections.singletonList(baseMap1)));

        replay(dispatcher);

        TableGenerator gtor = new TableGenerator(dispatcher, new MapGenerator(
                dispatcher, new MockIndicatorDAO()));
        gtor.generate(user, table, null, null);

        MapContent mapContent = map.getContent();
        Assert.assertNotNull("map content", mapContent);
        Assert.assertEquals("marker count", 1, mapContent.getMarkers().size());
        Assert.assertEquals("label on marker", "1",
                ((BubbleMapMarker) mapContent.getMarkers().get(0)).getLabel());

        Map<Integer, String> siteLabels = mapContent.siteLabelMap();
        Assert.assertEquals("site id in map", "1", siteLabels.get(1));

        SiteDTO row = table.getContent().getData().getRows().get(0);
        Assert.assertEquals("label on row", "1", row.get("map"));
    }

    private DispatcherSync createDispatcher() {
        DispatcherSync dispatcher = createMock(DispatcherSync.class);
        expect(dispatcher.execute(isA(GetSites.class))).andReturn(
                new SiteResult(dummySite())).anyTimes();
        replay(dispatcher);
        return dispatcher;
    }

    public SiteDTO dummySite() {
        SiteDTO site = new SiteDTO();
        site.setId(1);
        site.setLocationName("tampa bay");
        site.setIndicatorValue(INDICATOR_ID, 1500d);
        site.setX(28.4);
        site.setY(1.2);
        return site;
    }
}
