package org.activityinfo.ui.client.pageView.formClass;

import com.google.common.base.Function;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.dispatch.state.StateProvider;
import org.activityinfo.ui.client.widget.DisplayWidget;

import javax.annotation.Nullable;

/** *
 * Created by Mithun on 4/3/2014.
 */
public class DesignTab implements DisplayWidget<ResourceId> {

    private ResourceLocator resourceLocator;
    private StateProvider stateProvider;
    private FlowPanel panel;

    public DesignTab(ResourceLocator resourceLocator, StateProvider stateProvider) {
        this.resourceLocator = resourceLocator;
        this.stateProvider = stateProvider;
        this.panel = new FlowPanel();
    }

    @Override
    public Promise<Void> show(ResourceId resourceId) {
        return this.resourceLocator.getFormClass(resourceId)
                .then(new Function<FormClass, Void>() {
                    @Nullable
                    @Override
                    public Void apply(FormClass formClass) {
                        panel.add(new FormDesigner(resourceLocator, formClass, stateProvider).getFormDesignerPanel());
                        return null;
                    }
                });
    }

    @Override
    public Widget asWidget() {
        return panel;
    }
}
