/**
 * JAXB Mapping of the XForm Schema
 */
@XmlSchema(
        elementFormDefault = XmlNsForm.QUALIFIED,
        namespace = "http://www.w3.org/2002/xforms",
        xmlns = {
                @XmlNs(prefix = "", namespaceURI = Namespaces.XFORM),
                @XmlNs(prefix = "h", namespaceURI = Namespaces.XHTML),
                @XmlNs(prefix = "jr", namespaceURI = Namespaces.JAVA_ROSA)
        })
package org.activityinfo.io.xform.form;

import org.activityinfo.io.xform.Namespaces;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
