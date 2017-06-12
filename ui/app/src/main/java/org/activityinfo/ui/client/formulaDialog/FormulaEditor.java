package org.activityinfo.ui.client.formulaDialog;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.resources.ThemeStyles;
import org.activityinfo.analysis.ParsedFormula;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.promise.BiFunction;
import org.activityinfo.ui.codemirror.client.CodeMirrorEditor;
import org.activityinfo.ui.codemirror.client.CodeMirrorWidget;
import org.activityinfo.ui.codemirror.client.Pos;
import org.activityinfo.ui.codemirror.client.Token;

/**
 * Formula Editor based on CodeMirror
 */
public class FormulaEditor implements IsWidget {

    private CodeMirrorWidget widget;

    private Observable<FormTree> formTree;
    private StatefulValue<String> formulaText;

    private Observable<ParsedFormula> result;
    private Linter linter;

    public FormulaEditor(Observable<FormTree> formTree) {
        this.formTree = formTree;
        this.formulaText = new StatefulValue<>();

        widget = new CodeMirrorWidget() {
            @Override
            protected void onAttach() {
                super.onAttach();
                FormulaEditor.this.onAttach();
            }

            @Override
            protected void onDetach() {
                super.onDetach();
                FormulaEditor.this.onDetach();
            }
        };
        widget.addStyleName(ThemeStyles.get().style().border());

        this.result = Observable.transform(formTree, formulaText, new BiFunction<FormTree, String, ParsedFormula>() {
            @Override
            public ParsedFormula apply(FormTree tree, String formula) {
                return new ParsedFormula(tree, formula);
            }
        });
    }

    private void onAttach() {
        widget.addChangeHandler(() -> formulaText.updateValue(widget.getEditor().getDoc().getValue()));
        linter = new Linter(result, getEditor());
        linter.start();
    }


    private void onDetach() {
        // TODO: detach listener?
    }


    @Override
    public Widget asWidget() {
        return widget;
    }

    public CodeMirrorEditor getEditor() {
        return widget.getEditor();
    }


    public void insertAt(String expression, Pos insertPos) {

        Token[] lineTokens = widget.getEditor().getLineTokens(insertPos.line, true);
        int ch = insertPos.ch;

        // Find the tokens before and after the insert pos
        Token before = null;
        Token after = null;

        for (int i = 0; i < lineTokens.length; i++) {
            Token token = lineTokens[i];
            if(ch == token.getStart()) {
                if(i > 0) {
                    before = lineTokens[i-1];
                }
                after = token;
                break;
            } else if(ch > token.getStart() && ch < token.getEnd()) {
                before = token;
                after = token;
                break;
            } else if(ch == token.getEnd()) {
                before = token;
                if(i+1 < lineTokens.length) {
                    after = lineTokens[i+1];
                }
                break;
            }
        }

        // Add extra spaces for depending on the token type
        if(spaceBefore(before)) {
            expression = " " + expression;
        }
        if(spaceBefore(after)) {
            expression = expression + " ";
        }

        getEditor().getDoc().replaceRange(expression, insertPos, insertPos);
    }

    private boolean spaceBefore(Token before) {
        if(before == null) {
            return false;
        }
        switch (before.getType()) {
            case "bracket":
            case "whitespace":
                return false;
            default:
                return true;
        }
    }


    public void setValue(String expression) {
        formulaText.updateIfNotEqual(expression);
        getEditor().getDoc().setValue(expression);

    }

    public Observable<ParsedFormula> getValue() {
        return result;
    }
}
