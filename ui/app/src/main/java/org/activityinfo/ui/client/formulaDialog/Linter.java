package org.activityinfo.ui.client.formulaDialog;

import org.activityinfo.analysis.FieldReference;
import org.activityinfo.analysis.ParsedFormula;
import org.activityinfo.model.formula.FormulaError;
import org.activityinfo.model.formula.SourcePos;
import org.activityinfo.model.formula.SourceRange;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.codemirror.client.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides visual feedback on expression parsing and errors.
 */
public class Linter {

    private Observable<ParsedFormula> formula;
    private CodeMirrorEditor editor;
    private Subscription subscription;

    private List<TextMarker> markers = new ArrayList<>();


    public Linter(Observable<ParsedFormula> formula, CodeMirrorEditor editor) {
        this.formula = formula;
        this.editor = editor;
    }

    public void start() {
        this.subscription = formula.subscribe(this::onParsed);
    }

    private void onParsed(Observable<ParsedFormula> observable) {
        clearMarks();
        if(observable.isLoaded()) {
            applyMarks(observable.get());
        }
    }

    private void applyMarks(ParsedFormula formula) {
        if(!formula.isValid()) {
            for (FormulaError error : formula.getErrors()) {
                if(error.hasSourceRange()) {
                    SourceRange range = error.getSourceRange();
                    MarkOptions options = MarkOptions.create();
                    options.setClassName("CodeMirror-lint-mark-error");
                    options.setTitle(error.getMessage());

                    TextMarker marker = editor.getDoc().markText(pos(range.getStart()), pos(range.getEnd()), options);
                    markers.add(marker);
                }
            }
        } else {
            for (FieldReference fieldReference : formula.getReferences()) {
                SourceRange range = fieldReference.getSourceRange();
                MarkOptions options = MarkOptions.create();
                options.setClassName(FormulaResources.INSTANCE.styles().fieldAnnotation());
                options.setTitle(fieldReference.getDescription());

                TextMarker marker = editor.getDoc().markText(pos(range.getStart()), pos(range.getEnd()), options);
                markers.add(marker);
            }
        }
    }

    private Pos pos(SourcePos pos) {
        return CodeMirror.create(pos.getLine(), pos.getColumn());
    }

    private void clearMarks() {
        for (TextMarker marker : markers) {
            marker.clear();
        }
        markers.clear();
    }

    public void stop() {
        this.subscription.unsubscribe();
    }
}
