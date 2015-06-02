package org.activityinfo.geoadmin.merge2.view.swing.match;

import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.geotools.feature.FeatureCollection;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Style;
import org.geotools.swing.DefaultRenderingExecutor;
import org.geotools.swing.JMapPane;
import org.geotools.swing.RenderingExecutor;

import javax.swing.*;
import java.awt.*;


public class MatchMapPanel extends JPanel {

    public MatchMapPanel(ImportView view) {
        // Create a MapContent instance and add one or more layers to it
        MapContent map = new MapContent();

        FeatureCollection featureSource = null;
        Style style = null;
        Layer sourceLayer = new FeatureLayer(featureSource, style, "Source");

        // Create a renderer which will draw the features
        GTRenderer renderer = new StreamingRenderer();

        // Create the map pane to work with this renderer and map content.
        // When first shown on screen it will display the layers.
        RenderingExecutor renderingExecutor = new DefaultRenderingExecutor();

        JMapPane mapPane = new JMapPane(map, renderingExecutor, renderer);
        add(mapPane, BorderLayout.CENTER);

    }

}
