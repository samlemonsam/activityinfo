package org.activityinfo.ui.codemirror.client;

public interface Linter {

    LintingProblem[] lint(String text);

}
