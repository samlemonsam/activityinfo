package org.activityinfo.ui.codemirror.client;

import com.google.gwt.core.client.JavaScriptObject;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType
public class LintingProblem {

    private String message;
    private String severity;
    private Pos from;
    private Pos to;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Pos getFrom() {
        return from;
    }

    public void setFrom(Pos from) {
        this.from = from;
    }

    public Pos getTo() {
        return to;
    }

    public void setTo(Pos to) {
        this.to = to;
    }
}
