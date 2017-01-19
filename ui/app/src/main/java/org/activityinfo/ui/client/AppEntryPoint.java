package org.activityinfo.ui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sencha.gxt.widget.core.client.container.Viewport;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.ui.client.service.FormService;
import org.activityinfo.ui.client.service.FormServiceImpl;
import org.activityinfo.ui.client.table.TableModel;
import org.activityinfo.ui.client.table.TableView;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GWT EntryPoint that starts the application.
 */
public class AppEntryPoint implements EntryPoint {

    private static final Logger logger = Logger.getLogger(AppEntryPoint.class.getName());

    @Override
    public void onModuleLoad() {

        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable reason) {
                logger.log(Level.SEVERE, "Unable to start application", reason);
                Window.alert("Some error occurred while starting application");
            }

            @Override
            public void onSuccess() {

                FormService service = new FormServiceImpl();
                TableView view = new TableView(new TableModel(service, CuidAdapter.activityFormClass(33)));

                Viewport viewport = new Viewport();
                viewport.setWidget(view);
                RootLayoutPanel.get().add(viewport);
            }
        });
    }
}
