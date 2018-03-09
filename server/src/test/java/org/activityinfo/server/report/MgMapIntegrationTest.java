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
package org.activityinfo.server.report;

import org.activityinfo.TestOutput;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.legacy.shared.command.GenerateElement;
import org.activityinfo.legacy.shared.reports.content.MapContent;
import org.activityinfo.legacy.shared.reports.content.MapMarker;
import org.activityinfo.legacy.shared.reports.model.MapReportElement;
import org.activityinfo.legacy.shared.reports.model.clustering.AdministrativeLevelClustering;
import org.activityinfo.legacy.shared.reports.model.clustering.AutomaticClustering;
import org.activityinfo.legacy.shared.reports.model.layers.BubbleMapLayer;
import org.activityinfo.legacy.shared.reports.model.layers.IconMapLayer;
import org.activityinfo.legacy.shared.reports.model.layers.MapLayer;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.geo.TestGeometry;
import org.activityinfo.server.report.renderer.image.ImageMapRenderer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


@RunWith(InjectionSupport.class)
@Modules(ReportModule.class)
@OnDataSet("/dbunit/mg-mapping.db.xml")
public class MgMapIntegrationTest extends CommandTestCase2 {

    public static final int NUMBER_OF_BENE_INDICATOR_ID = 12445;
    public static final int MG_OWNER_ID = 1070;
    public static final int FOKOTANY_LEVEL_ID = 1512;

    private MapContent content;
    private MapReportElement map;

    @Before
    public final void setup() {
        setUser(MG_OWNER_ID);
    }

    @Test
    public void siteBoundsToAdminLevelsAreMappedAsBubbles() throws IOException {

        BubbleMapLayer layer = new BubbleMapLayer();
        layer.addIndicator(NUMBER_OF_BENE_INDICATOR_ID);

        generateMap(layer, "mg-bubbles");

        assertThat(content.getMarkers().size(), equalTo(10));

        MapMarker marker = getMarkerForSite(1336279918);
        assertThat(marker.getX(), equalTo(262));
        assertThat(marker.getY(), equalTo(113));
    }

    @Test
    public void siteBoundsToAdminLevelsAreMappedAsIcons() throws IOException {

        IconMapLayer layer = new IconMapLayer();
        layer.setIcon("educ");
        layer.getIndicatorIds().add(NUMBER_OF_BENE_INDICATOR_ID);

        generateMap(layer, "mg-icons");

        assertThat(content.getMarkers().size(), equalTo(10));

        MapMarker marker = getMarkerForSite(1336279918);
        assertThat(marker.getX(), equalTo(262));
        assertThat(marker.getY(), equalTo(113));
    }


    @Test
    public void siteBoundsToAdminLevelsMappedAsIconsAutoClustered() throws IOException {

        IconMapLayer layer = new IconMapLayer();
        layer.setIcon("educ");
        layer.getIndicatorIds().add(NUMBER_OF_BENE_INDICATOR_ID);
        layer.setClustering(new AutomaticClustering());

        generateMap(layer, "mg-icons-auto");

        assertTrue(content.getMarkers().size() > 0);
        assertTrue(content.getUnmappedSites().isEmpty());

    }

    @Test
    public void sitesBoundToAdminLevelsAreAutoClusteredProperly() throws IOException {

        BubbleMapLayer layer = new BubbleMapLayer();
        layer.addIndicator(NUMBER_OF_BENE_INDICATOR_ID);
        layer.setClustering(new AutomaticClustering());

        generateMap(layer, "mg-auto-cluster");

        assertThat(content.getMarkers().size(), equalTo(4));
        assertThat(content.getUnmappedSites().size(), equalTo(0));
    }

    @Test
    public void sitesBoundToAdminLevelsAreClusteredByAdminLevelMissingBounds() throws IOException {

        BubbleMapLayer layer = new BubbleMapLayer();
        layer.addIndicator(NUMBER_OF_BENE_INDICATOR_ID);
        layer.setClustering(clusterByFokotany());

        generateMap(layer, "mg-cluster-fok");

        assertThat(content.getMarkers().size(), equalTo(10));
        assertThat(content.getUnmappedSites().size(), equalTo(0));
    }

    private AdministrativeLevelClustering clusterByFokotany() {
        AdministrativeLevelClustering clustering = new AdministrativeLevelClustering();
        clustering.getAdminLevels().add(FOKOTANY_LEVEL_ID);
        return clustering;
    }

    private void generateMap(MapLayer layer, String fileName) throws IOException {
        map = new MapReportElement();
        map.addLayer(layer);

        content = (MapContent) execute(new GenerateElement(map));

        renderToFile(map, fileName);
    }

    private MapMarker getMarkerForSite(int siteId) {
        for(MapMarker marker : this.content.getMarkers()) {
            if(marker.getSiteIds().contains(siteId)) {
                return marker;
            }
        }
        throw new AssertionError("No marker for " + siteId);
    }

    private void renderToFile(MapReportElement map, String fileName) throws IOException {
        ImageMapRenderer renderer = new ImageMapRenderer(TestGeometry.get(), "src/main/webapp/mapicons");
        renderer.renderToFile(map, TestOutput.getFile(getClass(), fileName, ".png"));
    }
}
