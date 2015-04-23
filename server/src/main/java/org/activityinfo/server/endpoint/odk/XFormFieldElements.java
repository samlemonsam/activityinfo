package org.activityinfo.server.endpoint.odk;

import org.activityinfo.io.xform.form.Bind;
import org.activityinfo.io.xform.form.BodyElement;
import org.activityinfo.io.xform.form.InstanceElement;

import java.util.List;


public class XFormFieldElements {

    private InstanceElement instanceElement;
    private List<Bind> bindings;
    private List<BodyElement> bodyElements;

    public XFormFieldElements(InstanceElement instanceElement, List<Bind> bindings, List<BodyElement> bodyElements) {
        this.instanceElement = instanceElement;
        this.bindings = bindings;
        this.bodyElements = bodyElements;
    }
}
