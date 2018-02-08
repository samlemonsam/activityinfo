package org.activityinfo.ui.client.lookup.view;

import com.google.common.base.Strings;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.themebuilder.base.client.config.ThemeDetails;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.observable.Observable;

import java.util.List;

/**
 * Subclass of {@link ComboBoxCell} that adds a loading message for Observables as well as
 * empty text messages.
 */
class ChoiceCell extends ComboBoxCell<String> {


    interface Style extends CssResource {
        String message();
    }

    interface Templates extends XTemplates {
        @XTemplate("<div class=\'{style.message}\'>{message}</div>")
        SafeHtml renderMessage(Style style, String message);
    }

    interface Resources extends ClientBundle {
        @Source("choices.gss")
        Style style();

        ThemeDetails theme();
    }

    private static final int QUERY_DELAY_MS = 100;

    private static final Resources RESOURCES = GWT.create(Resources.class);

    private static final Templates TEMPLATES = GWT.create(Templates.class);

    private Observable<List<String>> observable;


    public ChoiceCell(Observable<List<String>> observable, ListStore<String> store) {
        super(store, key -> key);
        this.observable = observable;
        setForceSelection(true);
        setUseQueryCache(false);
        setQueryDelay(QUERY_DELAY_MS);
        setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        RESOURCES.style().ensureInjected();
    }

    @Override
    protected void onResultsLoad(Context context,
                                 XElement parent,
                                 ValueUpdater<String> updater,
                                 String value) {

        // Copied and modified from com.sencha.gxt.cell.core.client.form.ComboBoxCell
        // to reflect loading/empty state of the observable

        if (!hasFocus(context, parent)) {
            return;
        }

        boolean wasExpanded = isExpanded();
        if (!wasExpanded) {
            expand(context, parent, updater, value);
        }

        if(getListView().getElements().size() == 0) {
            XElement listElement = getListView().getElement();

            if (observable.isLoading()) {
                // We are still loading... Patientez SVP!
                listElement.setInnerSafeHtml(
                    TEMPLATES.renderMessage(RESOURCES.style(), I18N.CONSTANTS.loading()));

            } else if (getListView().getElements().size() == 0) {
                if (Strings.isNullOrEmpty(lastQuery)) {
                    // The referenced form has no records!
                    // Nothing to do.
                    listElement.setInnerSafeHtml(
                        TEMPLATES.renderMessage(RESOURCES.style(), I18N.CONSTANTS.noRecords()));
                } else {
                    // There may be records, but there aren't any matching what the
                    // user has been searching for.
                    listElement.setInnerSafeHtml(
                        TEMPLATES.renderMessage(RESOURCES.style(), I18N.CONSTANTS.noMatchingRecords()));

                }
            }
        }

        if(wasExpanded) {
            restrict(parent);
        }

        if (lastQuery != null && lastQuery.equals(getAllQuery())) {
            if (isEditable()) {
                selectAll(parent);
            }
        }

        // select an element in the listview based on the current text
        if (selectByValue(getText(parent)) == null) {
            select(0);
        }
    }

    @Override
    public void finishEditing(Element parent, String value, Object key, ValueUpdater<String> valueUpdater) {
        // NOOP
    }
}
