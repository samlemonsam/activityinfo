package chdc.frontend.client.entry;

import chdc.frontend.client.i18n.ChdcLabels;
import chdc.frontend.client.table.TablePlace;
import chdc.frontend.client.theme.*;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.ScrollSupport;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.ui.client.table.view.TableView;

public class DataEntryWidget implements IsWidget, HasSidebar {

    private final MainContainer container;

    public DataEntryWidget(RecordRef recordRef) {

        FlowLayoutContainer mainContent = new FlowLayoutContainer();
        mainContent.setScrollMode(ScrollSupport.ScrollMode.AUTOY);
        mainContent.setStyleName("maincontent");

        // Action bar
        IconButton saveButton = new IconButton(Icon.SAVE, I18N.CONSTANTS.save());
        LinkButton addAnotherLink = new LinkButton(ChdcLabels.LABELS.addAnotherIncident(),
                new DataEntryPlace(recordRef.getFormId()).toUri());
        LinkButton closeLink = new LinkButton(I18N.CONSTANTS.close(),
                new TablePlace(recordRef.getFormId()).toUri());


        FlowLayoutContainer secondary = new FlowLayoutContainer();
        secondary.setStyleName("actionbar__secondary");
        secondary.add(saveButton);

        FlowLayoutContainer primary = new FlowLayoutContainer();
        primary.setStyleName("actionbar__primary");
        primary.add(addAnotherLink);
        primary.add(closeLink);

        ActionBar actionBar = new ActionBar();
        actionBar.addShortcut(secondary);
        actionBar.addShortcut(primary);

        DataEntrySidebar sideBar = new DataEntrySidebar();

        this.container = new MainContainer();
        this.container.add(mainContent);
        this.container.add(actionBar);
        this.container.add(sideBar);
    }

    @Override
    public Widget asWidget() {
        return container;
    }
}
