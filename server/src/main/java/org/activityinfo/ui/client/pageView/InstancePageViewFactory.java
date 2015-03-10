package org.activityinfo.ui.client.pageView;

import com.google.common.base.Function;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.core.shared.application.FolderClass;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.ui.client.pageView.folder.FolderPageView;
import org.activityinfo.ui.client.pageView.formClass.DesignTab;
import org.activityinfo.ui.client.pageView.formClass.FormClassPageView;
import org.activityinfo.ui.client.pageView.formClass.TableTab;
import org.activityinfo.ui.client.widget.DisplayWidget;

/**
 * Creates a InstancePageView given a class id
 */
public class InstancePageViewFactory implements Function<InstanceViewModel, DisplayWidget<InstanceViewModel>> {

    private final ResourceLocator resourceLocator;

    public InstancePageViewFactory(ResourceLocator resourceLocator) {
        this.resourceLocator = resourceLocator;
    }

    @Override
    public InstancePageView apply(InstanceViewModel view) {
        ResourceId classId = view.getInstance().getClassId();
        if(classId.equals(FolderClass.CLASS_ID)) {
            return new FolderPageView(resourceLocator);
        } else if(classId.equals(FormClass.CLASS_ID)) {
            if("design".equals(view.getPath())) {
                return new FormClassPageView(new DesignTab(resourceLocator));
            } else {
                return new FormClassPageView(new TableTab(resourceLocator));
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
