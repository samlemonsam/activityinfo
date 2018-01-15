package chdc.frontend.client.entry;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModel;

/**
 * Specialized Form Panel for incidents
 */
public class IncidentFormPanel implements IsWidget {

    private final Label label = new Label();

    public IncidentFormPanel(Observable<FormInputViewModel> viewModel) {


    }

    @Override
    public Widget asWidget() {
        return label;
    }
}
