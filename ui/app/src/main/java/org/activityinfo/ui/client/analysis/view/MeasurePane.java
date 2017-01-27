package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.activityinfo.ui.client.analysis.view.measureDialog.model.MeasureSelectionModel;
import org.activityinfo.ui.client.analysis.view.measureDialog.view.MeasureDialog;
import org.activityinfo.ui.client.store.FormStore;


public class MeasurePane implements IsWidget {

    private ContentPanel contentPanel;
    private FormStore formStore;

    public MeasurePane(final FormStore formStore) {
        this.formStore = formStore;
        this.contentPanel = new ContentPanel();
        this.contentPanel.setHeading("Measures");
        this.contentPanel.setWidget(new HTML("Testing"));

        ToolButton addButton = new ToolButton(ToolButton.PLUS);
        addButton.addSelectHandler(new SelectEvent.SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                MeasureDialog dialog = new MeasureDialog(MeasurePane.this.formStore);
                dialog.show();
            }
        });
        this.contentPanel.addTool(addButton);

    }


    @Override
    public Widget asWidget() {
        return contentPanel;
    }
}
