package org.activityinfo.ui.client.page.print;


import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.ActivityInfoEntryPoint;
import org.activityinfo.ui.client.component.form.SimpleFormPanel;
import org.activityinfo.ui.client.component.form.VerticalFieldContainer;
import org.activityinfo.ui.client.component.form.field.FieldWidgetMode;
import org.activityinfo.ui.client.component.form.field.FormFieldWidgetFactory;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.dispatch.state.StateProvider;
import org.activityinfo.ui.client.style.BaseStylesheet;
import org.activityinfo.ui.client.widget.LoadingPanel;
import org.activityinfo.ui.client.widget.loading.PageLoadingPanel;

import javax.inject.Provider;

/**
 * Special panel that we attach to the root document on navigation to
 * #print/form/{formId}/{recordId}
 * 
 * <p>This needs to be add a top level, outside of all the normal application chrome, 
 * in order to print properly</p>
 */
public class PrintFormPanel extends FlowPanel {

    @Inject
    public PrintFormPanel(final ResourceLocator resourceLocator, final StateProvider stateProvider) {

        // Expect #print/form/{formId}/{recordId}
        String hash = Window.Location.getHash();
        String[] parts = hash.split("/");
        if(parts.length != 4) {
            Window.alert("Invalid URL");
            return;
        }

        final ResourceId formId = ResourceId.valueOf(parts[2]);
        final ResourceId recordId = ResourceId.valueOf(parts[3]);



        BaseStylesheet.INSTANCE.ensureInjected();
        
        Document.get().getBody().addClassName(BaseStylesheet.CONTAINER_STYLE);
        addStyleName("container");
                

        SimpleFormPanel formPanel = new SimpleFormPanel(
                resourceLocator,
                stateProvider,
                new VerticalFieldContainer.Factory(),
                new FormFieldWidgetFactory(resourceLocator, FieldWidgetMode.NORMAL));

        formPanel.setHeadingVisible(true);


        LoadingPanel<FormInstance> loadingPanel = new LoadingPanel<>(new PageLoadingPanel());
        loadingPanel.setDisplayWidget(formPanel);
        add(loadingPanel);

        
        loadingPanel.show(new Provider<Promise<FormInstance>>() {

            @Override
            public Promise<FormInstance> get() {
                return resourceLocator.getFormInstance(formId, recordId);
            }
        });

        ActivityInfoEntryPoint.hideLoadingIndicator();
    }

    public static void open(ResourceId formId, ResourceId recordId) {
        String printUrl = Window.Location.createUrlBuilder()
                .setHash("print/form/" + formId.asString() + "/" + recordId.asString()).buildString();

        Window.open(printUrl, "_blank", "");
    }
}
