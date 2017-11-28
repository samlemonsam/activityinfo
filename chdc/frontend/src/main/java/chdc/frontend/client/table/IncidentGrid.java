package chdc.frontend.client.table;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IncidentGrid implements IsWidget {

    private Grid<IncidentModel> grid;

    public IncidentGrid() {

        ListStore<IncidentModel> store = new ListStore<>(IncidentModel.PROPERTIES.id());

        ColumnConfig<IncidentModel, String> narrativeColumn = new ColumnConfig<>(IncidentModel.PROPERTIES.narrative(), 150, "Narrative");
        ColumnConfig<IncidentModel, Date> dateColumn = new ColumnConfig<>(IncidentModel.PROPERTIES.date(), 50, "Date");
        ColumnConfig<IncidentModel, String> timeColumn = new ColumnConfig<>(IncidentModel.PROPERTIES.time(), 10, "Time");
        ColumnConfig<IncidentModel, String> perpetrator = new ColumnConfig<>(IncidentModel.PROPERTIES.perpetrator(), 100, "Perpetrator");


        List<ColumnConfig<IncidentModel, ?>> columns = new ArrayList<>();
        columns.add(narrativeColumn);
        columns.add(dateColumn);
        columns.add(timeColumn);
        columns.add(perpetrator);

        ColumnModel<IncidentModel> columnModel = new ColumnModel<>(columns);

        grid = new Grid<>(store, columnModel);
    }

    @Override
    public Widget asWidget() {
        return grid;
    }
}
