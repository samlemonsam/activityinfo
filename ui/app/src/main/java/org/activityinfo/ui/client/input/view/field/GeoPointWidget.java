package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.io.match.coord.CoordinateAxis;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.ui.client.input.model.FieldInput;

/**
 * FieldWidget for {@link org.activityinfo.model.type.geo.GeoPointType} fields.
 */
public class GeoPointWidget implements FieldWidget {

    private FlowPanel flowPanel;
    private CoordinateField latitudeField;
    private CoordinateField longitudeField;

    public GeoPointWidget(FieldUpdater updater) {
        latitudeField = new CoordinateField(CoordinateAxis.LATITUDE);
        longitudeField = new CoordinateField(CoordinateAxis.LONGITUDE);

        latitudeField.addKeyUpHandler(event -> updater.update(input()));
        longitudeField.addKeyUpHandler(event -> updater.update(input()));

        flowPanel = new FlowPanel();
        flowPanel.add(latitudeField);
        flowPanel.add(longitudeField);
    }

    private FieldInput input() {
        if(latitudeField.isValid() && longitudeField.isValid()) {
            boolean latitudeEmpty = latitudeField.getValue() == null;
            boolean longitudeEmpty = longitudeField.getValue() == null;

            if(latitudeEmpty && longitudeEmpty) {
                return FieldInput.EMPTY;
            }
            if(!latitudeEmpty && !longitudeEmpty) {
                return new FieldInput(new GeoPoint(latitudeField.getValue(), longitudeField.getValue()));
            }
        }
        return FieldInput.INVALID_INPUT;
    }


    @Override
    public void setRelevant(boolean relevant) {
        latitudeField.setEnabled(relevant);
        longitudeField.setEnabled(relevant);
    }

    @Override
    public Widget asWidget() {
        return flowPanel;
    }
}
