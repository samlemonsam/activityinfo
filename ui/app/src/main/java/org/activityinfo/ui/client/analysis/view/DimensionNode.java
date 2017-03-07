package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.core.client.ValueProvider;
import org.activityinfo.ui.client.analysis.model.DimensionSource;

public abstract class DimensionNode {

    public static final ValueProvider<DimensionNode, String> VALUE_PROVIDER = new ValueProvider<DimensionNode, String>() {
        @Override
        public String getValue(DimensionNode object) {
            return object.getLabel();
        }

        @Override
        public void setValue(DimensionNode object, String value) {
        }

        @Override
        public String getPath() {
            return "label";
        }
    };


    public abstract String getKey();


    public abstract String getLabel();

    public abstract DimensionSource dimensionModel();

    public abstract ImageResource getIcon();
}
