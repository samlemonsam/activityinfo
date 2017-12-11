package chdc.frontend.client.theme;

import chdc.frontend.client.i18n.ChdcLabels;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.user.client.ui.Widget;

public class QuickSearchForm extends Widget {

    public QuickSearchForm() {
        FormElement form = Document.get().createFormElement();
        form.setClassName("quicksearch");
        form.setInnerSafeHtml(ChdcTemplates.TEMPLATES.quickSearchForm(ChdcLabels.LABELS));
        setElement(form);
    }
}
