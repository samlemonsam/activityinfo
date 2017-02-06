package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.button.ButtonBar;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.form.TextArea;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.formulaDialog.FormulaElement;
import org.activityinfo.ui.client.formulaDialog.FormulaElementCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the values of the calculation option
 */
public class FormulaPanel implements IsWidget {

    private final BorderLayoutContainer container;

    private final Observable<FormClass> formSchema;

    private ListView<FormulaElement, FormulaElement> fieldList;

    private final ListStore<FormulaElement> store;

    private TextArea formulaArea;


    public FormulaPanel(Observable<FormClass> formSchema) {
        this.formSchema = formSchema;

        // First a Button Bar with basic operations and plus buttons
        TextButton plusButton = new TextButton("+", event -> insert("+"));
        TextButton minusButton = new TextButton("-", event -> insert("-"));
        TextButton multiplyButton = new TextButton("*", event -> insert("*"));
        TextButton divideButton = new TextButton("/", event -> insert("/"));

        ButtonBar bar = new ButtonBar();
        bar.add(plusButton);
        bar.add(minusButton);
        bar.add(multiplyButton);
        bar.add(divideButton);


        // Finally the formula area where the text is

        formulaArea = new TextArea();

        ContentPanel formulaPanel = new ContentPanel();
        formulaPanel.setHeading("Formula");
        formulaPanel.setWidget(formulaArea);

        // Then a list of fields that can be added to the formula
        store = new ListStore<>(FormulaElement.KEY_PROVIDER);
        fieldList = new ListView<>(store, FormulaElement.VALUE_PROVIDER);
        fieldList.setCell(new FormulaElementCell());

        ContentPanel fieldPanel = new ContentPanel();
        fieldPanel.setHeading("Fields");
        fieldPanel.setWidget(fieldList);

        BorderLayoutContainer.BorderLayoutData fieldPanelLayout = new BorderLayoutContainer.BorderLayoutData(0.4);
        fieldPanelLayout.setCollapsible(true);
        fieldPanelLayout.setCollapseHeaderVisible(true);

        container = new BorderLayoutContainer();
        container.setCenterWidget(formulaPanel);
        container.setEastWidget(fieldPanel, fieldPanelLayout);

        formSchema.subscribe(this::onFormSchemaChanged);
        fieldList.getSelectionModel().addSelectionHandler(this::onFieldSelected);
    }


    private void onFormSchemaChanged(Observable<FormClass> formSchema) {
        if(formSchema.isLoaded()) {
            List<FormulaElement> elements = new ArrayList<>();
            for (FormField field : formSchema.get().getFields()) {
                elements.add(new FormulaElement(field));
            }
            store.replaceAll(elements);
        } else {
            store.clear();
        }
    }


    private void onFieldSelected(SelectionEvent<FormulaElement> event) {
        insert(event.getSelectedItem().getCode());
    }

    private void insert(String newText) {
        int insertPos = formulaArea.getCursorPos();
        String formula = formulaArea.getText();

        String before = formula.substring(0, insertPos);
        String after = formula.substring(insertPos);

        if(before.length() > 0 && !before.endsWith(" ")) {
            before += " ";
            insertPos++;
        }

        if(!after.startsWith(" ")) {
            after = " " + after;
            insertPos++;
        }

        formulaArea.setText(before + newText + after);
        formulaArea.setCursorPos(insertPos + newText.length());
        formulaArea.focus();
    }

    @Override
    public Widget asWidget() {
        return container;
    }
}
