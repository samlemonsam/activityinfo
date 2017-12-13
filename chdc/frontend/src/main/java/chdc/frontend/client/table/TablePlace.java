package chdc.frontend.client.table;

import com.google.gwt.place.shared.Place;

public class TablePlace extends Place {

    public static final String SLUG = "table";

    private final String formId;

    public TablePlace(String formId) {
        this.formId = formId;
    }

    @Override
    public String toString() {
        return "table/" + formId;
    }
}
