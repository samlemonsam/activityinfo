package chdc.frontend.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface ChdcResources extends ClientBundle {

    ChdcResources RESOURCES = GWT.create(ChdcResources.class);

    @Source("Chdc.gss")
    Style getStyle();

    interface Style extends CssResource {

        String banner();

        String sidebar();
    }

}
