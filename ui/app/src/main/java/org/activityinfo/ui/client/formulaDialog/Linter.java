/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
