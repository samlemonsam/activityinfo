package org.activityinfo.io.xform.form;

import org.activityinfo.io.xform.Namespaces;

import javax.xml.bind.annotation.*;

@XmlRootElement(namespace = Namespaces.XHTML, name = "html")
@XmlType(propOrder = {"head", "body"})
public class XForm {

    private Head head = new Head();
    private Body body = new Body();

    @XmlElement(namespace = Namespaces.XHTML)
    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    @XmlElement(namespace = Namespaces.XHTML)
    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

}
