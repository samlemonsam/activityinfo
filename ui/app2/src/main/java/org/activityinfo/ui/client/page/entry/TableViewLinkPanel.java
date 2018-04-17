package org.activityinfo.ui.client.page.entry;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.ui.client.App3;

public class TableViewLinkPanel implements SiteGridPanelView {

    private ContentPanel panel;

    TableViewLinkPanel(final ActivityFormDTO dto) {
        this.panel = new ContentPanel();
        panel.setHeaderVisible(false);
        panel.setLayout(new CenterLayout());
        panel.setBodyStyle("font-family: sans-serif;");
        panel.add(new Text(I18N.CONSTANTS.pleaseUseNewDataEntry()));

        Button navigate = new Button(I18N.CONSTANTS.tryNewDataEntryInterface());
        navigate.setScale(Style.ButtonScale.LARGE);
        navigate.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                App3.openNewTable(dto.getFormId());
            }
        });
        panel.addButton(navigate);
        panel.setButtonAlign(Style.HorizontalAlignment.CENTER);
    }

    @Override
    public void addSelectionChangeListener(SelectionChangedListener<SiteDTO> listener) {
        return;
    }

    @Override
    public void addDoubleClickListener(SelectionChangedListener<SiteDTO> listener) {
        return;
    }

    @Override
    public void refresh() {
        return;
    }

    @Override
    public Component asComponent() {
        return panel;
    }

    @Override
    public SiteDTO getSelection() {
        return null;
    }
}
