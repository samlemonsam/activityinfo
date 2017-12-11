package chdc.frontend.client.theme;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.TextInputCell;
import com.sencha.gxt.widget.core.client.form.TextField;

public class QuickSearchForm implements IsWidget {

    private final FormContainer form;
    private final TextField searchInput;
    private final SearchButton searchButton;

    public QuickSearchForm() {

        searchInput = new TextField(new TextInputCell(new SearchFieldAppearance()));
        searchInput.setWidth(-1);
        searchButton = new SearchButton();

        form = new FormContainer("quicksearch");
        form.add(searchInput);
        form.add(searchButton);
    }

    @Override
    public Widget asWidget() {
        return form;
    }
}
